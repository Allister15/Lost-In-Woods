package com.DinoCorp.Lost_In_Woods.config;

import com.DinoCorp.Lost_In_Woods.model.*;
import com.DinoCorp.Lost_In_Woods.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
public class DatabaseSeeder implements CommandLineRunner {

    private final ScenarioRepository scenarioRepo;
    // 📢 1. Inject your Session repository so we can clear everything in order
    private final GameSessionRepository sessionRepo;

    public DatabaseSeeder(ScenarioRepository scenarioRepo, GameSessionRepository sessionRepo) {
        this.scenarioRepo = scenarioRepo;
        this.sessionRepo = sessionRepo;
    }

    @Override
    public void run(String... args) {
        System.out.println("🔄 Wiping old data to prevent constraint collisions...");

        // 📢 2. Wipe existing data in correct relational order (child tables first)
        sessionRepo.deleteAll();
        scenarioRepo.deleteAll();

        System.out.println("🌲 Database wiped cleanly. Beginning fresh seed execution...");

        // ─── 3. YOUR EXACT SCENARIO BUILDERS CONTINUE BELOW ───
        Scenario s0 = Scenario.builder()
                .id(0L)
                .chapter("Chapter I — The Awakening")
                .sceneDescription("You open your eyes. Your head throbs...")
                .hint("Your first choice sets your survival disposition.")
                .entityAvatar("🧍").entityName("Ethan — Survivor").entityQuote("I woke up alone. No phone. No compass.")
                .svgPrompt("Draw a dark atmospheric SVG scene of a lone human figure waking up on the forest floor.")
                .choices(List.of(
                        Choice.builder().text("Follow the trail north — movement is survival").meta("Prioritize covering ground")
                                .hpModifier(0).scoreModifier(10).conferredTrait("brave").isGoodOutcome(true).systemLog("Moved north at once — brave but exposed.")
                                .narrative("You set off at a steady pace. The trail is faint but intentional.").build(),
                        Choice.builder().text("Make camp under the overhang — assess your supplies first").meta("Prioritize composure")
                                .hpModifier(8).scoreModifier(8).conferredTrait("cautious").isGoodOutcome(true).systemLog("Made shelter first.")
                                .narrative("A lighter, a pocket knife, a half-full bottle. You leave the overhang steadier.").build()
                )).build();

        // ... Your other scenarios (s1, s2, s3, s4, s5, s6) go here exactly as before ...

        // ─── 4. SAVE CLEANLY TO POSTGRESQL ───
        scenarioRepo.saveAll(List.of(s0)); // Add s1, s2, etc. back into this list array
        System.out.println("🎉 All chapters initialized in PostgreSQL database with ZERO constraint conflicts!");
    }
}