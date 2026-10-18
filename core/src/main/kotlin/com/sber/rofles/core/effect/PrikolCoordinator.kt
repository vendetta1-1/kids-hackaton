package com.sber.rofles.core.effect

import com.sber.rofles.core.bluetooth.HighFiveCoordinator
import com.sber.rofles.domain.model.Combination
import com.sber.rofles.domain.repository.ConfigRepository
import com.sber.rofles.domain.repository.HighFiveRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Координатор прикола: слушает тряску, проверяет ScreenGuard и включённость,
 * решает «соло или дай пять» и публикует состояние для глобального оверлея.
 */
@Singleton
class PrikolCoordinator @Inject constructor(
    private val shakeController: ShakeController,
    private val repository: HighFiveRepository,
    private val configRepository: ConfigRepository,
    private val highFive: HighFiveCoordinator,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    /** Номер аватарки пользователя. Пока заглушка, позже из профиля. */
    private val avatarNumber: Int = 0

    private val _effect = MutableStateFlow<EffectState?>(null)
    val effect: StateFlow<EffectState?> = _effect.asStateFlow()

    private val _currentScreen = MutableStateFlow(PrikolScreen.MAIN)

    /** Текущий экран, чтобы понять, разрешён ли прикол. Ставится из навигации. */
    var currentScreen: PrikolScreen
        get() = _currentScreen.value
        set(value) {
            _currentScreen.value = value
        }

    private var running = false

    fun start() {
        if (running) return
        running = true
        shakeController.start()
        highFive.start()

        // Чувствительность тряски — из конфига.
        scope.launch {
            configRepository.config().collect { config ->
                shakeController.threshold = config.shakeThreshold
            }
        }

        shakeController.shakes
            .onEach { handleShake() }
            .launchIn(scope)
    }

    private fun handleShake() {
        scope.launch {
            // Новая тряска во время эффекта игнорируется.
            if (_effect.value != null) return@launch
            _effect.value = processShake()
        }
    }

    /**
     * Решение «соло или дай пять» для текущей тряски. Вынесено отдельно,
     * чтобы покрыть юнит-тестами.
     */
    internal suspend fun processShake(): EffectState? {
        val screen = _currentScreen.value
        if (!ScreenGuard.allows(screen)) return null

        val enabled = configRepository.config().first().enabled
            && repository.isEnabled().first()
        if (!enabled) return null

        val element = repository.currentElement().first() ?: return null
        val config = configRepository.config().first()
        val animationUrl = config.elements.firstOrNull { it.element == element }?.lottieUrl

        // Пробуем рукопожатие: если рядом друг — «дай пять».
        val peer = highFive.shakeHands(element, avatarNumber)

        return if (peer != null && peer.peerElement != null) {
            val combination = Combination(element, peer.peerElement)
            repository.addCard(combination)
            EffectState(
                kind = EffectKind.HIGH_FIVE,
                element = element,
                combination = combination,
                animationUrl = animationUrl,
            )
        } else {
            EffectState(
                kind = EffectKind.SOLO,
                element = element,
                animationUrl = animationUrl,
            )
        }
    }

    /** Оверлей закончился — сбрасываем состояние. */
    fun dismiss() {
        _effect.value = null
    }

    fun stop() {
        shakeController.stop()
        highFive.stop()
        scope.cancel()
    }
}