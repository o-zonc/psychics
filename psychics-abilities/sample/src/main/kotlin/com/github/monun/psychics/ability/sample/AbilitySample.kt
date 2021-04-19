package com.github.monun.psychics.ability.sample

import com.github.monun.psychics.AbilityConcept
import com.github.monun.psychics.ActiveAbility
import com.github.monun.psychics.Channel
import com.github.monun.psychics.attribute.EsperAttribute
import com.github.monun.psychics.attribute.EsperStatistic
import com.github.monun.psychics.damage.Damage
import com.github.monun.psychics.damage.DamageType
import com.github.monun.psychics.util.hostileFilter
import com.github.monun.tap.effect.playFirework
import net.kyori.adventure.text.Component.text
import org.bukkit.*
import org.bukkit.entity.LivingEntity
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerEvent
import org.bukkit.inventory.ItemStack

class AbilityConceptSample : AbilityConcept() {
    init {
        cooldownTime = 5000L
        castingTime = 1000L
        range = 64.0
        cost = 10.0
        damage = Damage(DamageType.RANGED, EsperStatistic.of(EsperAttribute.ATTACK_DAMAGE to 1.0))
        description = listOf(
            text("지정한 대상에게 폭발을 일으킵니다.")
        )
        wand = ItemStack(Material.STICK)
    }
}

class AbilitySample : ActiveAbility<AbilityConceptSample>(), Listener {
    companion object {
        private val effect = FireworkEffect.builder().with(FireworkEffect.Type.BURST).withColor(Color.RED).build()

        private fun LivingEntity.playPsychicEffect() {
            world.playFirework(location, effect)
        }
    }

    override fun onInitialize() {
        targeter = {
            val player = esper.player
            val start = player.eyeLocation
            val world = start.world

            world.rayTrace(
                start,
                start.direction,
                concept.range,
                FluidCollisionMode.NEVER,
                true,
                0.5,
                player.hostileFilter()
            )?.hitEntity
        }
    }

    override fun onChannel(channel: Channel) {
        val player = esper.player
        val location = player.location.apply { y += 3.0 }

        location.world.spawnParticle(
            Particle.FLAME,
            location,
            20,
            0.0,
            0.0,
            0.0,
            0.2
        )
    }

    override fun onCast(event: PlayerEvent, action: WandAction, target: Any?) {
        if (target !is LivingEntity) return

        val concept = concept

        psychic.consumeMana(concept.cost)
        cooldownTime = concept.cooldownTime

        target.psychicDamage()
        target.playPsychicEffect()
    }
}