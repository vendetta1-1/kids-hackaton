package com.sber.rofles.core.prank

import com.sber.rofles.domain.model.BleCapability
import com.sber.rofles.domain.model.Element
import com.sber.rofles.domain.model.MyElement
import com.sber.rofles.domain.model.PrankEvent
import com.sber.rofles.domain.repository.Clock
import com.sber.rofles.domain.repository.MyElementRepository
import com.sber.rofles.domain.repository.PrankConfigRepository
import com.sber.rofles.domain.repository.PrankSettingsRepository
import com.sber.rofles.domain.repository.ShakeSource
import com.sber.rofles.domain.usecase.HighFiveUseCase
import com.sber.rofles.domain.usecase.ResolveSkin
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicLong
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PrankController @Inject constructor(
    private val configRepository: PrankConfigRepository,
    private val settingsRepository: PrankSettingsRepository,
    private val myElementRepository: MyElementRepository,
    private val shakeSource: ShakeSource,
    private val highFive: HighFiveUseCase,
    private val haptics: Haptics,
    private val clock: Clock,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val ids = AtomicLong()
    private var shakeJob: Job? = null
    private var configRefreshed = false

    private val _effect = MutableStateFlow<EffectUi?>(null)
    val effect: StateFlow<EffectUi?> = _effect.asStateFlow()

    private val _toast = MutableStateFlow<ToastUi?>(null)
    val toast: StateFlow<ToastUi?> = _toast.asStateFlow()

    private val _sheet = MutableStateFlow<SheetUi?>(null)
    val sheet: StateFlow<SheetUi?> = _sheet.asStateFlow()

    val picker: StateFlow<PickerUi?> = combine(configRepository.config, myElementRepository.observe()) { config, mine ->
        config ?: return@combine null
        val skin = ResolveSkin(config, clock.today())
        PickerUi(
            elements = Element.entries.map(skin::element),
            current = mine?.element,
            canChange = true,
            title = config.text("pickerTitle", "Выбери свою стихию"),
            subtitle = config.text("pickerSubtitle", "Когда вы дадите пять, она смешается со стихией друга"),
            done = config.text("pickerDone", "Готово"),
            tomorrow = config.text("pickerTomorrow", "Сменить можно завтра"),
        )
    }.stateIn(scope, SharingStarted.Eagerly, null)

    val bluetoothTexts: StateFlow<BluetoothTexts> = configRepository.config.map { config ->
        fun text(key: String, default: String) = config?.text(key, default) ?: default
        BluetoothTexts(
            title = text("btTitle", "Давай дадим пять!"),
            text = text("btText", "Включи Bluetooth, чтобы телефоны нашли друг друга"),
            enable = text("btEnable", "Включить Bluetooth"),
            later = text("btLater", "Не сейчас"),
        )
    }.stateIn(scope, SharingStarted.Eagerly, BluetoothTexts("Давай дадим пять!", "", "Включить Bluetooth", "Не сейчас"))

    @OptIn(ExperimentalCoroutinesApi::class)
    fun shakes(): Flow<Long> = configRepository.config.flatMapLatest { config ->
        if (config == null || !config.enabled) emptyFlow() else shakeSource.shakes(config.shake)
    }

    fun refreshConfigOnce() {
        if (configRefreshed) return
        configRefreshed = true
        scope.launch { configRepository.refresh() }
    }

    fun onShake(at: Long) {
        if (_sheet.value != null || shakeJob?.isActive == true) return
        shakeJob = scope.launch { highFive(at).collect(::handle) }
    }

    fun openElementPicker() {
        _sheet.value = SheetUi.ElementPicker(firstTime = false)
    }

    fun selectElement(element: Element) {
        val sheet = _sheet.value as? SheetUi.ElementPicker
        scope.launch {
            val current = myElementRepository.observe().first()
            if (current?.element != element) myElementRepository.save(MyElement(element, clock.today()))
            _sheet.value = null
            if (sheet?.firstTime == true) playSolo(element)
        }
    }

    fun dismissSheet() {
        if (_sheet.value is SheetUi.Bluetooth) markBluetoothOnboardingShown()
        _sheet.value = null
    }

    fun onBluetoothFlowFinished() {
        markBluetoothOnboardingShown()
        _sheet.value = null
    }

    fun dismissEffect(id: Long) {
        _effect.compareAndSet(_effect.value?.takeIf { it.id == id }, null)
    }

    fun dismissToast(id: Long) {
        _toast.compareAndSet(_toast.value?.takeIf { it.id == id }, null)
    }

    private suspend fun handle(event: PrankEvent) {
        when (event) {
            is PrankEvent.Solo -> show(event)
            is PrankEvent.HighFive -> {
                show(event)
                val caption = if (event.isNew) {
                    text("toastNew", "Новая карточка!")
                } else {
                    "${text("toastRepeat", "Снова вместе")} · ×${event.count}"
                }
                _toast.value = ToastUi(ids.incrementAndGet(), caption, event.combo.title, event.combo.key, event.isNew)
                vibrate()
            }
            is PrankEvent.Friendship -> {
                show(event)
                _toast.value = ToastUi(ids.incrementAndGet(), text("toastFriendship", "Вы дали пять!"), event.title, null, false)
                vibrate()
            }
            PrankEvent.NeedElement -> _sheet.value = SheetUi.ElementPicker(firstTime = true)
            is PrankEvent.BluetoothUnavailable -> {
                val askable = event.capability == BleCapability.NO_PERMISSION || event.capability == BleCapability.OFF
                if (askable && !settingsRepository.settings.first().bluetoothOnboardingShown) {
                    _sheet.value = SheetUi.Bluetooth(event.capability)
                }
            }
        }
    }

    private fun playSolo(element: Element) {
        val config = configRepository.config.value ?: return
        show(PrankEvent.Solo(ResolveSkin(config, clock.today()).element(element)))
    }

    private fun show(event: PrankEvent) {
        _effect.value = EffectUi(ids.incrementAndGet(), event)
    }

    private suspend fun vibrate() {
        if (settingsRepository.settings.first().vibrationEnabled) haptics.highFive()
    }

    private fun text(key: String, default: String): String =
        configRepository.config.value?.text(key, default) ?: default

    private fun markBluetoothOnboardingShown() {
        scope.launch { settingsRepository.setBluetoothOnboardingShown() }
    }
}
