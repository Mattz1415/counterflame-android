package com.scarlet.counterflame

import com.scarlet.counterflame.settings.AppSettings
import com.scarlet.counterflame.simulation.SimulatedEntity
import com.scarlet.counterflame.simulation.Team
import com.scarlet.counterflame.simulation.WorldSimulation
import org.junit.Assert.*
import org.junit.Test

class CompanionUnitTest {

    @Test
    fun testSimulatedEntityMovement() {
        val entity = SimulatedEntity(
            id = 1,
            name = "s1mple",
            team = Team.COUNTER_TERRORIST,
            worldX = 0f,
            worldY = 0f,
            worldZ = 10f,
            velocityX = 5f,
            velocityZ = 2f,
            health = 100f
        )

        entity.updatePosition(1.0f) // 1 second
        assertEquals(5.0f, entity.worldX, 0.001f)
        assertEquals(12.0f, entity.worldZ, 0.001f)
    }

    @Test
    fun testWorldSimulationInit() {
        val sim = WorldSimulation()
        sim.initialize(count = 6)
        assertEquals(6, sim.entities.size)

        // Verify team distribution
        val tCount = sim.entities.count { it.team == Team.TERRORIST }
        val ctCount = sim.entities.count { it.team == Team.COUNTER_TERRORIST }
        assertEquals(3, tCount)
        assertEquals(3, ctCount)
    }

    @Test
    fun testAppSettingsDefaults() {
        val settings = AppSettings()
        assertTrue(settings.espEnabled)
        assertTrue(settings.showBoundingBoxes)
        assertTrue(settings.showPlayerNames)
        assertTrue(settings.showHealthBar)
        assertTrue(settings.aimAssistEnabled)
        assertEquals(180f, settings.aimFovRadius, 0.001f)
        assertEquals(6.0f, settings.aimSmoothness, 0.001f)
        assertEquals(0, settings.targetPriority)
    }
}
