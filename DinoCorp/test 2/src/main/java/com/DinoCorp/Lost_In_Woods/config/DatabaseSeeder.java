package com.DinoCorp.Lost_In_Woods.config;

import com.DinoCorp.Lost_In_Woods.model.*;
import com.DinoCorp.Lost_In_Woods.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
public class DatabaseSeeder implements CommandLineRunner {

    private final ScenarioRepository scenarioRepo;
    private final GameSessionRepository sessionRepo;

    public DatabaseSeeder(ScenarioRepository scenarioRepo, GameSessionRepository sessionRepo) {
        this.scenarioRepo = scenarioRepo;
        this.sessionRepo = sessionRepo;
    }

    @Override
    public void run(String... args) {
        System.out.println("🔄 Wiping old data...");
        sessionRepo.deleteAll();
        scenarioRepo.deleteAll();
        System.out.println("🌲 Seeding scenarios...");

        Scenario s0 = Scenario.builder()
                .id(0L)
                .chapter("Chapter I — The Awakening")
                .sceneDescription("You open your eyes. Pine needles press into your cheek. Your head throbs and the last thing you remember is the hiking trail — two days ago. No phone. No compass. Just the trees, closing in from every direction.")
                .hint("Your first choice sets your survival disposition.")
                .entityAvatar("🧍").entityName("Ethan — Survivor").entityQuote("I woke up alone. No phone. No compass.")
                .svgPrompt("Dark atmospheric SVG scene of a lone human figure waking up on a forest floor surrounded by tall pine trees, mist, dawn light.")
                .choices(List.of(
                        Choice.builder().text("Follow the trail north — movement is survival").meta("Prioritize covering ground")
                                .hpModifier(0).scoreModifier(10).conferredTrait("brave").isGoodOutcome(true)
                                .systemLog("Moved north at once — brave but exposed.")
                                .narrative("You set off at a steady pace. The trail is faint but intentional. You cover good ground before noon.").build(),
                        Choice.builder().text("Make camp under the overhang — assess your supplies first").meta("Prioritize composure")
                                .hpModifier(8).scoreModifier(8).conferredTrait("cautious").isGoodOutcome(true)
                                .systemLog("Made shelter and took stock first.")
                                .narrative("A lighter, a pocket knife, a half-full bottle. You leave the overhang steadier and better prepared.").build()
                )).build();

        Scenario s1 = Scenario.builder()
                .id(1L)
                .chapter("Chapter II — The River")
                .sceneDescription("You hear it before you see it — a fast-moving river cutting across your path. The water is cold and dark. Upstream you spot a log crossing, but it looks slippery. Downstream, the banks narrow and the current slows.")
                .hint("Water sources can be lifelines or death traps.")
                .entityAvatar("🌊").entityName("The River").entityQuote("I give and I take.")
                .svgPrompt("SVG scene of a rushing forest river at dusk with a mossy log crossing and dark water downstream.")
                .choices(List.of(
                        Choice.builder().text("Cross on the log — fastest route forward").meta("Accept the risk")
                                .hpModifier(-10).scoreModifier(12).conferredTrait("daring").isGoodOutcome(true)
                                .systemLog("Crossed the log — slipped midway but made it.")
                                .narrative("Halfway across, your boot slips. You catch yourself but plunge one arm into the freezing current. Cold and shaken, you reach the other bank.").build(),
                        Choice.builder().text("Wade downstream where the current is slower").meta("Methodical approach")
                                .hpModifier(-5).scoreModifier(10).conferredTrait("methodical").isGoodOutcome(true)
                                .systemLog("Waded across safely — slower but dry.")
                                .narrative("The water only reaches your knees. You cross safely and press on, clothes damp but spirits intact.").build(),
                        Choice.builder().text("Drink deeply and camp by the bank for the night").meta("Rest and hydrate")
                                .hpModifier(10).scoreModifier(5).conferredTrait("patient").isGoodOutcome(true)
                                .systemLog("Camped by river — recovered health.")
                                .narrative("You purify water with your lighter-warmed canteen, sleep well, and wake refreshed. The crossing can wait until dawn.").build()
                )).build();

        Scenario s2 = Scenario.builder()
                .id(2L)
                .chapter("Chapter III — The Stranger")
                .sceneDescription("A figure steps out from behind a spruce tree. Weathered jacket, grey beard, a hunting rifle slung over one shoulder. He studies you without speaking. His dog sits obediently at his heel.")
                .hint("Not every person in the wild is a threat — but trust must be earned.")
                .entityAvatar("🧔").entityName("Old Marsh — Hunter").entityQuote("What are you doing this deep in my woods, friend?")
                .svgPrompt("SVG scene of a rugged old hunter with a rifle and dog standing in a dense green forest, facing the viewer.")
                .choices(List.of(
                        Choice.builder().text("Tell him the truth — you are lost and need help").meta("Be honest")
                                .hpModifier(15).scoreModifier(15).conferredTrait("honest").isGoodOutcome(true)
                                .systemLog("Told the truth — earned trust and supplies.")
                                .narrative("He nods slowly, reaches into his pack and hands you dried venison and a folded map. 'Trail's two miles east,' he says. 'Don't stray again.'").build(),
                        Choice.builder().text("Stay vague — claim you are on a planned solo hike").meta("Stay guarded")
                                .hpModifier(0).scoreModifier(8).conferredTrait("guarded").isGoodOutcome(true)
                                .systemLog("Stayed vague — no supplies given but no trouble either.")
                                .narrative("He shrugs, points east, and disappears back into the trees. You get direction but nothing else.").build(),
                        Choice.builder().text("Back away slowly and hide — you don't know him").meta("Avoid contact")
                                .hpModifier(-5).scoreModifier(5).conferredTrait("paranoid").isGoodOutcome(false)
                                .systemLog("Avoided stranger — missed critical supply opportunity.")
                                .narrative("You retreat into the brush. Hours later, hunger gnaws at you. That was probably your best chance at help today.").build()
                )).build();

        Scenario s3 = Scenario.builder()
                .id(3L)
                .chapter("Chapter IV — The Storm")
                .sceneDescription("The sky turns green-grey in minutes. Wind strips leaves from the canopy above you. A full storm is about to hit — you have maybe ten minutes before it breaks.")
                .hint("Improvised shelter beats none at all.")
                .entityAvatar("⛈️").entityName("The Storm").entityQuote("Nobody outwalks me.")
                .svgPrompt("SVG scene of a dark stormy forest with dramatic green-grey sky, lightning in the distance, trees bending in wind.")
                .choices(List.of(
                        Choice.builder().text("Dig into the hillside and construct a lean-to from branches").meta("Build emergency shelter")
                                .hpModifier(5).scoreModifier(15).conferredTrait("resourceful").isGoodOutcome(true)
                                .systemLog("Built lean-to — sheltered safely through the storm.")
                                .narrative("It's cramped and leaks in two places but it holds. You emerge damp but functional when the storm passes two hours later.").build(),
                        Choice.builder().text("Keep moving and outrun the worst of it").meta("Push through")
                                .hpModifier(-20).scoreModifier(5).conferredTrait("reckless").isGoodOutcome(false)
                                .systemLog("Ran through storm — took serious damage.")
                                .narrative("The rain hits like gravel. You're soaked, freezing, and disoriented. You stop shaking an hour later, but at real cost.").build(),
                        Choice.builder().text("Find a large tree and wait it out under the canopy").meta("Use the environment")
                                .hpModifier(-5).scoreModifier(10).conferredTrait("adaptive").isGoodOutcome(true)
                                .systemLog("Sheltered under canopy — moderate outcome.")
                                .narrative("The canopy blocks most of it. You're damp and cold but uninjured when it passes.").build()
                )).build();

        Scenario s4 = Scenario.builder()
                .id(4L)
                .chapter("Chapter V — The Signal")
                .sceneDescription("You spot a helicopter in the distance — a search pattern. It's moving away from you. You have thirty seconds to do something visible before it disappears behind the ridge.")
                .hint("Rescue opportunities are brief. Act decisively.")
                .entityAvatar("🚁").entityName("Search Crew").entityQuote("We've been looking for three days.")
                .svgPrompt("SVG scene of a distant rescue helicopter over a forest ridge at golden hour, viewed from below through tree canopy.")
                .choices(List.of(
                        Choice.builder().text("Use your lighter to ignite dry leaves — create a smoke signal").meta("Make smoke")
                                .hpModifier(0).scoreModifier(20).conferredTrait("signal-smart").isGoodOutcome(true)
                                .systemLog("Smoke signal spotted by helicopter.")
                                .narrative("White smoke curls up through the canopy. The helicopter banks hard left. It's coming back.").build(),
                        Choice.builder().text("Sprint into the nearest clearing and wave your jacket").meta("Visual signal")
                                .hpModifier(-5).scoreModifier(18).conferredTrait("bold").isGoodOutcome(true)
                                .systemLog("Visual signal seen — helicopter circling.")
                                .narrative("You burst into the clearing, arms pumping. The crew spots you. They radio your position.").build(),
                        Choice.builder().text("Shout as loud as you can — they might have a spotter").meta("Audio signal")
                                .hpModifier(0).scoreModifier(5).conferredTrait("hopeful").isGoodOutcome(false)
                                .systemLog("Shouting not heard over rotors — helicopter missed.")
                                .narrative("Your voice is swallowed by the rotor wash and the canopy. The helicopter disappears over the ridge.").build()
                )).build();

        Scenario s5 = Scenario.builder()
                .id(5L)
                .chapter("Chapter VI — The Predator")
                .sceneDescription("A black bear steps onto the trail thirty metres ahead. It hasn't seen you yet. It's sniffing at something on the ground — possibly your earlier camp's scent trail.")
                .hint("Bear encounters are about appearing non-threatening and non-prey.")
                .entityAvatar("🐻").entityName("Black Bear").entityQuote("...")
                .svgPrompt("SVG scene of a large black bear on a forest trail at dusk, head down sniffing the ground, dark trees on both sides.")
                .choices(List.of(
                        Choice.builder().text("Back away slowly, keep eye contact, speak calmly").meta("Correct bear protocol")
                                .hpModifier(0).scoreModifier(20).conferredTrait("composed").isGoodOutcome(true)
                                .systemLog("Correct response — bear moved on without incident.")
                                .narrative("The bear looks up, huffs once, and ambles off the trail. You wait five minutes, then continue.").build(),
                        Choice.builder().text("Freeze completely and wait for it to leave").meta("Stay still")
                                .hpModifier(-5).scoreModifier(12).conferredTrait("fearful").isGoodOutcome(true)
                                .systemLog("Froze — bear came closer but eventually left.")
                                .narrative("The bear wanders toward you, sniffs the air, then loses interest. Your legs are shaking but you're unharmed.").build(),
                        Choice.builder().text("Run — get as much distance as possible now").meta("Flee instinct")
                                .hpModifier(-25).scoreModifier(0).conferredTrait("panicked").isGoodOutcome(false)
                                .systemLog("Running triggered chase response — badly injured.")
                                .narrative("The bear charges instantly. You run faster than you've ever run. You make it up a tree but not before taking a swipe across your back.").build()
                )).build();

        Scenario s6 = Scenario.builder()
                .id(6L)
                .chapter("Chapter VII — The Edge")
                .sceneDescription("You can see the treeline. Beyond it, a road. A car passes in the far distance. You are fifty metres from the end of the forest — but between you and the road is a steep rocky drop of about four metres with no clear path down.")
                .hint("You are almost out. Don't throw it away now.")
                .entityAvatar("🛤️").entityName("The Road").entityQuote("Almost there.")
                .svgPrompt("SVG scene showing the edge of a dense forest opening onto a road in warm evening light, with a steep rocky ledge in the foreground.")
                .choices(List.of(
                        Choice.builder().text("Find anchor points and lower yourself down carefully using your jacket as a rope").meta("Technical descent")
                                .hpModifier(0).scoreModifier(20).conferredTrait("precise").isGoodOutcome(true)
                                .systemLog("Safe technical descent — arrived uninjured.")
                                .narrative("It takes fifteen careful minutes. Your jacket tears but holds. You land softly and step onto the road.").build(),
                        Choice.builder().text("Walk the ridge until you find a safer descent point").meta("Scout first")
                                .hpModifier(5).scoreModifier(15).conferredTrait("thorough").isGoodOutcome(true)
                                .systemLog("Found safer path — arrived safely with bonus health.")
                                .narrative("Ten minutes of walking finds a natural slope. You walk out of the woods like it's a Sunday stroll.").build(),
                        Choice.builder().text("Jump — it's only four metres and you're almost out").meta("Leap of faith")
                                .hpModifier(-30).scoreModifier(5).conferredTrait("impulsive").isGoodOutcome(false)
                                .systemLog("Jumped — badly injured on landing.")
                                .narrative("You hit the rocks at an angle. A sharp pain in your ankle. You crawl to the road. A passing driver calls you an ambulance.").build()
                )).build();

        scenarioRepo.saveAll(List.of(s0, s1, s2, s3, s4, s5, s6));
        System.out.println("🎉 All 7 chapters seeded successfully!");
    }
}
