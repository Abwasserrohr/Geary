package com.mineinabyss.geary.minecraft

import com.mineinabyss.geary.ecs.api.engine.Engine
import com.mineinabyss.geary.ecs.api.services.GearyServiceProvider
import com.mineinabyss.geary.ecs.api.services.GearyServices
import com.mineinabyss.geary.ecs.systems.ExpiringComponentSystem
import com.mineinabyss.geary.ecs.systems.PassiveActionsSystem
import com.mineinabyss.geary.minecraft.access.BukkitEntityAccess
import com.mineinabyss.geary.minecraft.components.PlayerComponent
import com.mineinabyss.geary.minecraft.dsl.GearyLoadManager
import com.mineinabyss.geary.minecraft.dsl.GearyLoadPhase
import com.mineinabyss.geary.minecraft.dsl.attachToGeary
import com.mineinabyss.geary.minecraft.engine.SpigotEngine
import com.mineinabyss.idofront.commands.execution.ExperimentalCommandDSL
import com.mineinabyss.idofront.plugin.registerEvents
import com.mineinabyss.idofront.plugin.registerService
import kotlinx.serialization.InternalSerializationApi
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
import kotlin.reflect.KClass
import kotlin.time.ExperimentalTime

public class GearyPlugin : JavaPlugin() {
    @InternalSerializationApi
    @ExperimentalCommandDSL
    @ExperimentalTime
    override fun onEnable() {
        logger.info("On enable has been called")
        saveDefaultConfig()
        reloadConfig()
        GearyServices.setServiceProvider(object : GearyServiceProvider {
            override fun <T : Any> getService(service: KClass<T>): T? {
                return Bukkit.getServer().servicesManager.load(service.java)
            }
        })

        registerService<Engine>(SpigotEngine().apply { start() })

        // Register commands.
        GearyCommands()

        registerEvents(
            BukkitEntityAccess
        )

        // This will also register a serializer for GearyEntityType
        attachToGeary {
            autoscanComponents()
            autoscanConditions()
            autoscanActions()

            systems(
                PassiveActionsSystem,
                ExpiringComponentSystem,
            )

            bukkitEntityAccess {
                onEntityRegister<Player> { player ->
                    add(PlayerComponent(player.uniqueId))
                }
            }

            startup {
                GearyLoadPhase.ENABLE {
                    Bukkit.getOnlinePlayers().forEach { player ->
                        BukkitEntityAccess.registerEntity(player)
                    }
                }
            }
        }

        GearyLoadManager.onEnable()
    }

    override fun onDisable() {
        super.onDisable()
        logger.info("onDisable has been invoked!")
        server.scheduler.cancelTasks(this)
    }
}
