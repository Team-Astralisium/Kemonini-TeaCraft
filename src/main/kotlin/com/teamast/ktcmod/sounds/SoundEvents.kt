package com.teamast.ktcmod.sounds

import com.teamast.ktcmod.KTCMod
import net.minecraft.core.registries.Registries
import net.minecraft.sounds.SoundEvent
import net.neoforged.bus.api.IEventBus
import net.neoforged.neoforge.registries.DeferredHolder
import net.neoforged.neoforge.registries.DeferredRegister

object SoundEvents {
    @JvmStatic
    val SOUND_EVENTS = DeferredRegister.create(Registries.SOUND_EVENT, KTCMod.MODID)

    /** 携带体食用音效在 sounds.json 里的键，同时也是音效事件的 id。 */
    const val CARRIER_USE_SOUND_NAME: String = "item.carrier.use"

    @JvmStatic
    val DISC_SOUNDS: MutableList<DeferredHolder<SoundEvent, SoundEvent>> = mutableListOf()

    init {
        for (i in 1..21) {
            val discMusicName = "disc_music_$i"
            val discSound = SOUND_EVENTS.register(discMusicName) { id ->
                SoundEvent.createVariableRangeEvent(id)
            }
            DISC_SOUNDS += discSound
        }
    }

    /**
     * 食用病毒携带体的音效，事件 id 即 [CARRIER_USE_SOUND_NAME]。
     *
     * 对应的 sounds.json 条目写在 `src/main/resources/assets/kemono_teatime/sounds.json` 里，
     * 声音文件目前是**静音占位** `assets/kemono_teatime/sounds/item/carrier_use.ogg`：
     * 拿到正式音效后直接替换该 ogg 即可，代码与 json 都不用改。
     */
    @JvmStatic
    val CARRIER_USE: DeferredHolder<SoundEvent, SoundEvent> =
        SOUND_EVENTS.register(CARRIER_USE_SOUND_NAME) { id ->
            SoundEvent.createVariableRangeEvent(id)
        }

    fun getDiscSounds(i: Int): SoundEvent = DISC_SOUNDS[i - 1].get()

    fun register(eventBus: IEventBus) {
        SOUND_EVENTS.register(eventBus)
        KTCMod.LOGGER.info("Registering Sound Events for " + KTCMod.MODID + "...")
    }
}
