package com.teamast.ktcmod.datagen

import com.teamast.ktcmod.KTCMod
import com.teamast.ktcmod.sounds.SoundEvents
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import net.minecraft.data.CachedOutput
import net.minecraft.data.DataProvider
import net.minecraft.data.PackOutput
import java.util.concurrent.CompletableFuture

/**
 * 声音数据生成器，产出 `assets/kemono_teatime/sounds.json`（21 个唱片音效 + 携带体食用音效）。
 *
 * 注意 sounds.json 位于命名空间目录的**根**上，不属于任何子目录，所以不能用
 * [PackOutput.createPathProvider] 拼路径：它的 kind 参数会被当成子目录，传空串会拼出以 `/`
 * 开头的路径，被 `Identifier#resolveAgainst` 当成盘符绝对路径，最终写到盘符根目录
 * （`D:\sounds.json`）而不是资源包里。这里直接拼输出目录。
 */
class KTCSoundsProvider(output: PackOutput) : DataProvider {

    companion object {
        private const val DISC_COUNT = 21
    }

    /** 数据生成输出目录下的 `assets/kemono_teatime/sounds.json`。 */
    private val soundsJsonPath = output.getOutputFolder(PackOutput.Target.RESOURCE_PACK)
        .resolve(KTCMod.MODID)
        .resolve("sounds.json")

    override fun run(cache: CachedOutput): CompletableFuture<*> {
        val root = JsonObject()
        for (index in 1..DISC_COUNT) {
            root.add(discSoundName(index), soundEntry("${KTCMod.MODID}:records/${discSoundName(index)}"))
        }
        // 携带体食用音效（`item.carrier.use`）：目前指向静音占位 ogg，换正式音效只需替换同名文件
        root.add(SoundEvents.CARRIER_USE_SOUND_NAME, soundEntry("${KTCMod.MODID}:item/carrier_use"))
        return DataProvider.saveStable(cache, root, soundsJsonPath)
    }

    override fun getName(): String = "Sounds - ${KTCMod.MODID}"

    private fun discSoundName(index: Int): String = "disc_music_$index"

    /** 生成一条 sounds.json 条目：一个音效事件对应单个声音文件。 */
    private fun soundEntry(path: String): JsonObject {
        val sounds = JsonArray()
        sounds.add(path)
        val entry = JsonObject()
        entry.add("sounds", sounds)
        return entry
    }
}
