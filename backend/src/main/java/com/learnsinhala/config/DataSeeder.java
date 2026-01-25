package com.learnsinhala.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import com.learnsinhala.model.Category;
import com.learnsinhala.model.Difficulty;
import com.learnsinhala.model.Vocabulary;
import com.learnsinhala.repository.VocabularyRepository;

import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * Seeds 50 common Sri Lankan spoken Sinhala phrases.
 * Only runs in 'dev' profile and when database is empty.
 *
 * Categories covered:
 * - Greetings & Responses
 * - Daily Phrases
 * - Food & Drinks
 * - Shopping
 * - Travel & Directions
 * - Questions
 * - Time expressions
 * - Emergency phrases
 */
@Configuration
@Slf4j
public class DataSeeder {

    @Bean
    @Profile("dev")
    CommandLineRunner seedVocabulary(VocabularyRepository vocabularyRepository) {
        return args -> {
            if (vocabularyRepository.count() > 0) {
                log.info("Vocabulary data already exists, skipping seed");
                return;
            }

            log.info("Seeding 50 common Sinhala spoken phrases...");

            List<Vocabulary> phrases = List.of(

                // ========================================
                // GREETINGS - Essential daily greetings
                // ========================================

                Vocabulary.builder()
                    .sinhala("ayubowan")
                    .pronunciation("a-yu-bo-wan")
                    .tamil("vanakkam")
                    .english("Hello / Greetings")
                    .category(Category.GREETINGS)
                    .difficulty(Difficulty.BEGINNER)
                    .exampleSinhala("Ayubowan, kohomada?")
                    .exampleEnglish("Hello, how are you?")
                    .notes("Traditional greeting meaning 'may you live long'. Used formally.")
                    .tags(List.of("formal", "greeting", "essential"))
                    .build(),

                Vocabulary.builder()
                    .sinhala("kohomada")
                    .pronunciation("ko-ho-ma-da")
                    .tamil("eppadi irukkeenga")
                    .english("How are you?")
                    .category(Category.GREETINGS)
                    .difficulty(Difficulty.BEGINNER)
                    .exampleSinhala("Mahatthaya, kohomada?")
                    .exampleEnglish("Sir, how are you?")
                    .notes("Most common greeting in spoken Sinhala. Very versatile.")
                    .tags(List.of("greeting", "question", "essential"))
                    .build(),

                Vocabulary.builder()
                    .sinhala("honda, istuti")
                    .pronunciation("hon-da, is-tu-ti")
                    .tamil("nallam, nandri")
                    .english("Fine, thank you")
                    .category(Category.RESPONSES)
                    .difficulty(Difficulty.BEGINNER)
                    .exampleSinhala("Honda, istuti. Oya kohomada?")
                    .exampleEnglish("Fine, thank you. How are you?")
                    .notes("Standard polite response to 'kohomada'")
                    .tags(List.of("response", "polite", "essential"))
                    .build(),

                Vocabulary.builder()
                    .sinhala("subha udesanak")
                    .pronunciation("su-ba u-de-sa-nak")
                    .tamil("kaalai vanakkam")
                    .english("Good morning")
                    .category(Category.GREETINGS)
                    .difficulty(Difficulty.BEGINNER)
                    .exampleSinhala("Subha udesanak mahatthaya!")
                    .exampleEnglish("Good morning sir!")
                    .notes("Used until around noon")
                    .tags(List.of("greeting", "morning", "formal"))
                    .build(),

                Vocabulary.builder()
                    .sinhala("subha rathiriyak")
                    .pronunciation("su-ba ra-thi-ri-yak")
                    .tamil("iravu vanakkam")
                    .english("Good night")
                    .category(Category.GREETINGS)
                    .difficulty(Difficulty.BEGINNER)
                    .exampleSinhala("Honda, subha rathiriyak!")
                    .exampleEnglish("Okay, good night!")
                    .notes("Said when parting at night or before sleep")
                    .tags(List.of("greeting", "night", "parting"))
                    .build(),

                // ========================================
                // DAILY PHRASES - Most used expressions
                // ========================================

                Vocabulary.builder()
                    .sinhala("bohoma istuti")
                    .pronunciation("bo-ho-ma is-tu-ti")
                    .tamil("romba nandri")
                    .english("Thank you very much")
                    .category(Category.DAILY_PHRASES)
                    .difficulty(Difficulty.BEGINNER)
                    .exampleSinhala("Udaw karapu ekata bohoma istuti")
                    .exampleEnglish("Thank you very much for helping")
                    .notes("'Bohoma' intensifies - means 'very much'")
                    .tags(List.of("polite", "thanks", "essential"))
                    .build(),

                Vocabulary.builder()
                    .sinhala("karunakara")
                    .pronunciation("ka-ru-na-ka-ra")
                    .tamil("thayavu seithu")
                    .english("Please")
                    .category(Category.DAILY_PHRASES)
                    .difficulty(Difficulty.BEGINNER)
                    .exampleSinhala("Karunakara, meka ganna")
                    .exampleEnglish("Please, take this")
                    .notes("Essential for polite requests. Often shortened to 'karunaka' in speech.")
                    .tags(List.of("polite", "request", "essential"))
                    .build(),

                Vocabulary.builder()
                    .sinhala("samawenna")
                    .pronunciation("sa-ma-wen-na")
                    .tamil("mannikanum")
                    .english("Sorry / Excuse me")
                    .category(Category.DAILY_PHRASES)
                    .difficulty(Difficulty.BEGINNER)
                    .exampleSinhala("Samawenna, mama parakku una")
                    .exampleEnglish("Sorry, I was late")
                    .notes("Used for apologies and to get attention")
                    .tags(List.of("polite", "apology", "essential"))
                    .build(),

                Vocabulary.builder()
                    .sinhala("kamak naha")
                    .pronunciation("ka-mak na-ha")
                    .tamil("paravayillai")
                    .english("No problem / It's okay")
                    .category(Category.RESPONSES)
                    .difficulty(Difficulty.BEGINNER)
                    .exampleSinhala("Kamak naha, inna")
                    .exampleEnglish("No problem, stay")
                    .notes("Common response to apologies or thanks")
                    .tags(List.of("response", "casual", "essential"))
                    .build(),

                Vocabulary.builder()
                    .sinhala("ow")
                    .pronunciation("ow")
                    .tamil("aamam")
                    .english("Yes")
                    .category(Category.RESPONSES)
                    .difficulty(Difficulty.BEGINNER)
                    .exampleSinhala("Ow, mama enawa")
                    .exampleEnglish("Yes, I'm coming")
                    .notes("Informal yes. For respect, use 'ow mahatthaya' (yes sir)")
                    .tags(List.of("response", "essential"))
                    .build(),

                Vocabulary.builder()
                    .sinhala("naha")
                    .pronunciation("na-ha")
                    .tamil("illai")
                    .english("No")
                    .category(Category.RESPONSES)
                    .difficulty(Difficulty.BEGINNER)
                    .exampleSinhala("Naha, mata epa")
                    .exampleEnglish("No, I don't want it")
                    .notes("Spoken form. Written Sinhala uses 'naethi'")
                    .tags(List.of("response", "essential"))
                    .build(),

                Vocabulary.builder()
                    .sinhala("gihin ennam")
                    .pronunciation("gi-hin en-nam")
                    .tamil("poitu varen")
                    .english("I'll go and come back / Goodbye")
                    .category(Category.GREETINGS)
                    .difficulty(Difficulty.BEGINNER)
                    .exampleSinhala("Honda, gihin ennam!")
                    .exampleEnglish("Okay, see you later!")
                    .notes("Common way to say goodbye. Response: 'yanna enna' (go and come)")
                    .tags(List.of("parting", "casual", "essential"))
                    .build(),

                Vocabulary.builder()
                    .sinhala("yanna enna")
                    .pronunciation("yan-na en-na")
                    .tamil("poitu vaa")
                    .english("Go and come back (goodbye response)")
                    .category(Category.RESPONSES)
                    .difficulty(Difficulty.BEGINNER)
                    .exampleSinhala("Honda, yanna enna!")
                    .exampleEnglish("Okay, take care!")
                    .notes("Said in response to 'gihin ennam'")
                    .tags(List.of("parting", "response", "essential"))
                    .build(),

                // ========================================
                // QUESTIONS - Common question patterns
                // ========================================

                Vocabulary.builder()
                    .sinhala("mokakda")
                    .pronunciation("mo-kak-da")
                    .tamil("enna")
                    .english("What?")
                    .category(Category.QUESTIONS)
                    .difficulty(Difficulty.BEGINNER)
                    .exampleSinhala("Meka mokakda?")
                    .exampleEnglish("What is this?")
                    .notes("Basic question word. 'Mokakda karanne?' = What are you doing?")
                    .tags(List.of("question", "essential"))
                    .build(),

                Vocabulary.builder()
                    .sinhala("koheda")
                    .pronunciation("ko-he-da")
                    .tamil("enga")
                    .english("Where?")
                    .category(Category.QUESTIONS)
                    .difficulty(Difficulty.BEGINNER)
                    .exampleSinhala("Oya koheda yanney?")
                    .exampleEnglish("Where are you going?")
                    .notes("Very common question in Sri Lanka")
                    .tags(List.of("question", "essential"))
                    .build(),

                Vocabulary.builder()
                    .sinhala("kawuda")
                    .pronunciation("ka-wu-da")
                    .tamil("yaaru")
                    .english("Who?")
                    .category(Category.QUESTIONS)
                    .difficulty(Difficulty.BEGINNER)
                    .exampleSinhala("Eya kawuda?")
                    .exampleEnglish("Who is that?")
                    .notes("Can also be 'kauda' in casual speech")
                    .tags(List.of("question", "essential"))
                    .build(),

                Vocabulary.builder()
                    .sinhala("aiy")
                    .pronunciation("ai")
                    .tamil("yen")
                    .english("Why?")
                    .category(Category.QUESTIONS)
                    .difficulty(Difficulty.BEGINNER)
                    .exampleSinhala("Aiy enne naha?")
                    .exampleEnglish("Why aren't you coming?")
                    .notes("Short and commonly used in spoken Sinhala")
                    .tags(List.of("question", "essential"))
                    .build(),

                Vocabulary.builder()
                    .sinhala("kiyada")
                    .pronunciation("ki-ya-da")
                    .tamil("evvalavu")
                    .english("How much?")
                    .category(Category.QUESTIONS)
                    .difficulty(Difficulty.BEGINNER)
                    .exampleSinhala("Meka kiyada?")
                    .exampleEnglish("How much is this?")
                    .notes("Essential for shopping. Also 'kiyek da' for countable items.")
                    .tags(List.of("question", "shopping", "essential"))
                    .build(),

                Vocabulary.builder()
                    .sinhala("kavadada")
                    .pronunciation("ka-wa-da-da")
                    .tamil("eppo")
                    .english("When?")
                    .category(Category.QUESTIONS)
                    .difficulty(Difficulty.INTERMEDIATE)
                    .exampleSinhala("Oya kavadada enney?")
                    .exampleEnglish("When are you coming?")
                    .notes("Used for asking about time/dates")
                    .tags(List.of("question", "time"))
                    .build(),

                // ========================================
                // FOOD & DRINKS - Restaurant & daily use
                // ========================================

                Vocabulary.builder()
                    .sinhala("mata bath ona")
                    .pronunciation("ma-ta bath o-na")
                    .tamil("enakku saadam venum")
                    .english("I want rice")
                    .category(Category.FOOD)
                    .difficulty(Difficulty.BEGINNER)
                    .exampleSinhala("Mata bath ekak curry ekka ona")
                    .exampleEnglish("I want one rice with curry")
                    .notes("'Bath' is the staple food. 'Bath ekak' = one plate of rice")
                    .tags(List.of("food", "order", "essential"))
                    .build(),

                Vocabulary.builder()
                    .sinhala("te ekak denna")
                    .pronunciation("te e-kak den-na")
                    .tamil("oru tea kudunga")
                    .english("Give me one tea")
                    .category(Category.FOOD)
                    .difficulty(Difficulty.BEGINNER)
                    .exampleSinhala("Plain te ekak denna")
                    .exampleEnglish("Give me a plain tea")
                    .notes("Sri Lankan tea culture. 'Kiri te' = milk tea")
                    .tags(List.of("drink", "order", "essential"))
                    .build(),

                Vocabulary.builder()
                    .sinhala("watura tikak denna")
                    .pronunciation("wa-tu-ra ti-kak den-na")
                    .tamil("konjam thanneer kudunga")
                    .english("Give me some water")
                    .category(Category.FOOD)
                    .difficulty(Difficulty.BEGINNER)
                    .exampleSinhala("Karunakara, watura tikak denna")
                    .exampleEnglish("Please, give me some water")
                    .notes("'Tikak' means 'a little' or 'some'")
                    .tags(List.of("drink", "request", "essential"))
                    .build(),

                Vocabulary.builder()
                    .sinhala("kanna ona")
                    .pronunciation("kan-na o-na")
                    .tamil("saapida venum")
                    .english("Want to eat")
                    .category(Category.FOOD)
                    .difficulty(Difficulty.BEGINNER)
                    .exampleSinhala("Mata kanna ona")
                    .exampleEnglish("I want to eat")
                    .notes("'Kanna' = to eat. Very commonly used.")
                    .tags(List.of("food", "want", "essential"))
                    .build(),

                Vocabulary.builder()
                    .sinhala("bada ginnen")
                    .pronunciation("ba-da gin-nen")
                    .tamil("pasi edukkuthu")
                    .english("I'm hungry")
                    .category(Category.FOOD)
                    .difficulty(Difficulty.INTERMEDIATE)
                    .exampleSinhala("Bada ginnen, kanna yanada?")
                    .exampleEnglish("I'm hungry, shall we go eat?")
                    .notes("Literally 'stomach is burning'. Colloquial expression.")
                    .tags(List.of("food", "feeling", "colloquial"))
                    .build(),

                Vocabulary.builder()
                    .sinhala("pirichchata karanna")
                    .pronunciation("pi-rich-cha-ta ka-ran-na")
                    .tamil("kaaram panni")
                    .english("Make it spicy")
                    .category(Category.FOOD)
                    .difficulty(Difficulty.INTERMEDIATE)
                    .exampleSinhala("Bath eka pirichchata karanna")
                    .exampleEnglish("Make the rice spicy")
                    .notes("Sri Lankan food is often spicy. 'Pirith naha' = not spicy")
                    .tags(List.of("food", "preference"))
                    .build(),

                Vocabulary.builder()
                    .sinhala("bill eka denna")
                    .pronunciation("bill e-ka den-na")
                    .tamil("bill kudunga")
                    .english("Give me the bill")
                    .category(Category.FOOD)
                    .difficulty(Difficulty.BEGINNER)
                    .exampleSinhala("Karunakara, bill eka denna")
                    .exampleEnglish("Please, give me the bill")
                    .notes("Used in restaurants and shops")
                    .tags(List.of("restaurant", "payment", "essential"))
                    .build(),

                // ========================================
                // SHOPPING - Market and store phrases
                // ========================================

                Vocabulary.builder()
                    .sinhala("meka kiyada?")
                    .pronunciation("me-ka ki-ya-da")
                    .tamil("ithu evvalavu?")
                    .english("How much is this?")
                    .category(Category.SHOPPING)
                    .difficulty(Difficulty.BEGINNER)
                    .exampleSinhala("Mahatthaya, meka kiyada?")
                    .exampleEnglish("Sir, how much is this?")
                    .notes("Essential shopping phrase. Point at item while asking.")
                    .tags(List.of("shopping", "price", "essential"))
                    .build(),

                Vocabulary.builder()
                    .sinhala("godak wedi")
                    .pronunciation("go-dak we-di")
                    .tamil("romba athigam")
                    .english("Too expensive")
                    .category(Category.SHOPPING)
                    .difficulty(Difficulty.BEGINNER)
                    .exampleSinhala("Naha, godak wedi!")
                    .exampleEnglish("No, it's too expensive!")
                    .notes("Use this to negotiate prices. Common in markets.")
                    .tags(List.of("shopping", "bargaining", "essential"))
                    .build(),

                Vocabulary.builder()
                    .sinhala("adu karanna")
                    .pronunciation("a-du ka-ran-na")
                    .tamil("kammiya pannunga")
                    .english("Reduce the price")
                    .category(Category.SHOPPING)
                    .difficulty(Difficulty.INTERMEDIATE)
                    .exampleSinhala("Tikak adu karanna, boss")
                    .exampleEnglish("Reduce it a bit, boss")
                    .notes("Bargaining is common in Sri Lankan markets")
                    .tags(List.of("shopping", "bargaining"))
                    .build(),

                Vocabulary.builder()
                    .sinhala("mata meka ona")
                    .pronunciation("ma-ta me-ka o-na")
                    .tamil("enakku ithu venum")
                    .english("I want this")
                    .category(Category.SHOPPING)
                    .difficulty(Difficulty.BEGINNER)
                    .exampleSinhala("Mata meka ona, pack karanna")
                    .exampleEnglish("I want this, pack it")
                    .notes("Point while saying. 'Pack karanna' = please pack it")
                    .tags(List.of("shopping", "buying", "essential"))
                    .build(),

                Vocabulary.builder()
                    .sinhala("balanna puluwan da")
                    .pronunciation("ba-lan-na pu-lu-wan da")
                    .tamil("paarkkalama")
                    .english("Can I see/look?")
                    .category(Category.SHOPPING)
                    .difficulty(Difficulty.INTERMEDIATE)
                    .exampleSinhala("Eka balanna puluwan da?")
                    .exampleEnglish("Can I see that?")
                    .notes("Polite way to ask to examine items")
                    .tags(List.of("shopping", "request"))
                    .build(),

                // ========================================
                // TRAVEL & DIRECTIONS
                // ========================================

                Vocabulary.builder()
                    .sinhala("mata yanna ona")
                    .pronunciation("ma-ta yan-na o-na")
                    .tamil("enakku poga venum")
                    .english("I need to go")
                    .category(Category.TRAVEL)
                    .difficulty(Difficulty.BEGINNER)
                    .exampleSinhala("Mata Colombo yanna ona")
                    .exampleEnglish("I need to go to Colombo")
                    .notes("Basic travel phrase. Add destination before 'yanna'")
                    .tags(List.of("travel", "essential"))
                    .build(),

                Vocabulary.builder()
                    .sinhala("bus eka koheda?")
                    .pronunciation("bus e-ka ko-he-da")
                    .tamil("bus enga?")
                    .english("Where is the bus?")
                    .category(Category.TRAVEL)
                    .difficulty(Difficulty.BEGINNER)
                    .exampleSinhala("Kandy bus eka koheda?")
                    .exampleEnglish("Where is the Kandy bus?")
                    .notes("Buses are main transport. Say destination + 'bus eka'")
                    .tags(List.of("travel", "transport", "essential"))
                    .build(),

                Vocabulary.builder()
                    .sinhala("kellin yanna")
                    .pronunciation("kel-lin yan-na")
                    .tamil("neraga po")
                    .english("Go straight")
                    .category(Category.DIRECTIONS)
                    .difficulty(Difficulty.BEGINNER)
                    .exampleSinhala("Methanin kellin yanna")
                    .exampleEnglish("Go straight from here")
                    .notes("Basic direction. 'Methanin' = from here")
                    .tags(List.of("directions", "essential"))
                    .build(),

                Vocabulary.builder()
                    .sinhala("dakkunata harina")
                    .pronunciation("dak-ku-na-ta ha-ri-na")
                    .tamil("valathupakkam thiru")
                    .english("Turn right")
                    .category(Category.DIRECTIONS)
                    .difficulty(Difficulty.INTERMEDIATE)
                    .exampleSinhala("Signal eke dakkunata harina")
                    .exampleEnglish("Turn right at the signal")
                    .notes("'Dakkuna' = right side")
                    .tags(List.of("directions"))
                    .build(),

                Vocabulary.builder()
                    .sinhala("wamata harina")
                    .pronunciation("wa-ma-ta ha-ri-na")
                    .tamil("idathupakkam thiru")
                    .english("Turn left")
                    .category(Category.DIRECTIONS)
                    .difficulty(Difficulty.INTERMEDIATE)
                    .exampleSinhala("Pansala lagin wamata harina")
                    .exampleEnglish("Turn left near the temple")
                    .notes("'Wama' = left side. 'Pansala' = temple")
                    .tags(List.of("directions"))
                    .build(),

                Vocabulary.builder()
                    .sinhala("methana nawatanna")
                    .pronunciation("me-tha-na na-wa-tan-na")
                    .tamil("inga niluthunga")
                    .english("Stop here")
                    .category(Category.TRAVEL)
                    .difficulty(Difficulty.BEGINNER)
                    .exampleSinhala("Driver, methana nawatanna!")
                    .exampleEnglish("Driver, stop here!")
                    .notes("Essential for buses and three-wheelers")
                    .tags(List.of("travel", "transport", "essential"))
                    .build(),

                Vocabulary.builder()
                    .sinhala("dura kiyak da?")
                    .pronunciation("du-ra ki-yak da")
                    .tamil("dooram evvalavu?")
                    .english("How far is it?")
                    .category(Category.TRAVEL)
                    .difficulty(Difficulty.INTERMEDIATE)
                    .exampleSinhala("Station ekata dura kiyak da?")
                    .exampleEnglish("How far is it to the station?")
                    .notes("'Dura' = distance/far")
                    .tags(List.of("travel", "question"))
                    .build(),

                // ========================================
                // TIME EXPRESSIONS
                // ========================================

                Vocabulary.builder()
                    .sinhala("daan")
                    .pronunciation("daan")
                    .tamil("ippo")
                    .english("Now")
                    .category(Category.TIME)
                    .difficulty(Difficulty.BEGINNER)
                    .exampleSinhala("Daan yanawa")
                    .exampleEnglish("Going now")
                    .notes("Very common time word")
                    .tags(List.of("time", "essential"))
                    .build(),

                Vocabulary.builder()
                    .sinhala("ada")
                    .pronunciation("a-da")
                    .tamil("indru")
                    .english("Today")
                    .category(Category.TIME)
                    .difficulty(Difficulty.BEGINNER)
                    .exampleSinhala("Ada kama hodai")
                    .exampleEnglish("Today the food is good")
                    .notes("Basic time reference")
                    .tags(List.of("time", "essential"))
                    .build(),

                Vocabulary.builder()
                    .sinhala("heta")
                    .pronunciation("he-ta")
                    .tamil("naalai")
                    .english("Tomorrow")
                    .category(Category.TIME)
                    .difficulty(Difficulty.BEGINNER)
                    .exampleSinhala("Heta enawa")
                    .exampleEnglish("Coming tomorrow")
                    .notes("Commonly used for future plans")
                    .tags(List.of("time", "essential"))
                    .build(),

                Vocabulary.builder()
                    .sinhala("iye")
                    .pronunciation("i-ye")
                    .tamil("netru")
                    .english("Yesterday")
                    .category(Category.TIME)
                    .difficulty(Difficulty.BEGINNER)
                    .exampleSinhala("Iye eya aawa")
                    .exampleEnglish("He/She came yesterday")
                    .notes("Basic past time reference")
                    .tags(List.of("time", "essential"))
                    .build(),

                Vocabulary.builder()
                    .sinhala("welawa kiyada?")
                    .pronunciation("we-la-wa ki-ya-da")
                    .tamil("neram enna?")
                    .english("What time is it?")
                    .category(Category.TIME)
                    .difficulty(Difficulty.INTERMEDIATE)
                    .exampleSinhala("Samawenna, welawa kiyada?")
                    .exampleEnglish("Excuse me, what time is it?")
                    .notes("Polite way to ask time")
                    .tags(List.of("time", "question"))
                    .build(),

                // ========================================
                // EMERGENCY & IMPORTANT PHRASES
                // ========================================

                Vocabulary.builder()
                    .sinhala("mata udaw karanna")
                    .pronunciation("ma-ta u-daw ka-ran-na")
                    .tamil("enakku help pannunga")
                    .english("Help me")
                    .category(Category.EMERGENCY)
                    .difficulty(Difficulty.BEGINNER)
                    .exampleSinhala("Karunakara, mata udaw karanna!")
                    .exampleEnglish("Please, help me!")
                    .notes("Important emergency phrase")
                    .tags(List.of("emergency", "essential"))
                    .build(),

                Vocabulary.builder()
                    .sinhala("police eka ganna")
                    .pronunciation("police e-ka gan-na")
                    .tamil("police kuppidu")
                    .english("Call the police")
                    .category(Category.EMERGENCY)
                    .difficulty(Difficulty.INTERMEDIATE)
                    .exampleSinhala("Kauruwat police eka ganna!")
                    .exampleEnglish("Someone call the police!")
                    .notes("Emergency phrase. Police emergency: 119")
                    .tags(List.of("emergency", "safety"))
                    .build(),

                Vocabulary.builder()
                    .sinhala("mata sambada naha")
                    .pronunciation("ma-ta sam-ba-da na-ha")
                    .tamil("enakku comfortable illai")
                    .english("I'm not feeling well")
                    .category(Category.EMERGENCY)
                    .difficulty(Difficulty.INTERMEDIATE)
                    .exampleSinhala("Mata sambada naha, doctor kennek ona")
                    .exampleEnglish("I'm not well, I need a doctor")
                    .notes("Use when sick. 'Hospital ekata yanna ona' = need to go to hospital")
                    .tags(List.of("emergency", "health"))
                    .build(),

                Vocabulary.builder()
                    .sinhala("mata therenne naha")
                    .pronunciation("ma-ta the-ren-ne na-ha")
                    .tamil("enakku puriyala")
                    .english("I don't understand")
                    .category(Category.DAILY_PHRASES)
                    .difficulty(Difficulty.BEGINNER)
                    .exampleSinhala("Samawenna, mata therenne naha")
                    .exampleEnglish("Sorry, I don't understand")
                    .notes("Very useful when learning. Say slowly and clearly.")
                    .tags(List.of("learning", "essential"))
                    .build(),

                Vocabulary.builder()
                    .sinhala("ayet kiyanna")
                    .pronunciation("a-yet ki-yan-na")
                    .tamil("thirumba sollunga")
                    .english("Say it again")
                    .category(Category.DAILY_PHRASES)
                    .difficulty(Difficulty.BEGINNER)
                    .exampleSinhala("Karunakara, ayet kiyanna")
                    .exampleEnglish("Please, say it again")
                    .notes("Helpful when learning. 'Himin kiyanna' = say slowly")
                    .tags(List.of("learning", "essential"))
                    .build(),

                Vocabulary.builder()
                    .sinhala("mama Sinhala igena ganawa")
                    .pronunciation("ma-ma sin-ha-la i-ge-na ga-na-wa")
                    .tamil("naan Sinhala padikkiren")
                    .english("I am learning Sinhala")
                    .category(Category.DAILY_PHRASES)
                    .difficulty(Difficulty.INTERMEDIATE)
                    .exampleSinhala("Mama Sinhala igena ganawa, himin kathaa karanna")
                    .exampleEnglish("I'm learning Sinhala, please speak slowly")
                    .notes("People will appreciate your effort and help you!")
                    .tags(List.of("learning", "introduction"))
                    .build()
            );

            vocabularyRepository.saveAll(phrases);
            log.info("Seeded {} spoken Sinhala phrases", phrases.size());
        };
    }
}
