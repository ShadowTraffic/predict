(ns io.shadowtraffic.predict.ollama
  (:require [clojure.string :as s]
            [cheshire.core :as json]
            [clj-http.client :as http]))

(def examples
  [["Inputs: Sophia Martinez | Emma Martinez |  Amelia Johnson |  Ava Miller |  Isabella Garcia"
    "Correct Selection: Name.fullName"]

   ["Inputs: 999 Pine Lane, Fairview WA, 73301 | 649 Maple Street, Madison FL, 10001 | 907 Cedar Road, Madison TX, 73301 | 771 Cedar Road, Riverside WA, 33101 | 823 Elm Drive, Springfield NY, 33101"
    "Correct Selection: Address.fullAddress"]

   ["Inputs: 2008-01-24 | 2017-07-19 | 2006-03-18 | 2004-06-17 | 2002-06-21"
    "Correct Selection: Date.past"]

   ["Inputs: 53fb04a9-da12-400d-a00d-21b7b86f0701 | 05e1775f-8ae8-4cda-bba9-6827329764e0 | 9fabf6a4-8ea0-4d5e-aad2-8e41af3c2cfb | 648ca573-084d-4f8b-86df-e4af0478e82d | 900b42d5-5300-436d-80b0-d8186a1449c1"
    "Correct Selection: Internet.uuid"]

   ["Inputs: 70.58.207.53 | 125.165.188.212 | 234.51.186.152 | 146.130.214.200 | 232.201.197.166"
    "Correct Selection: Internet.ipV4Cidr"]

   ["Inputs: Pennsylvania | New Jersey | California | New York | Texas"
    "Correct Selection: Address.state"]

   ["Inputs: Nebula Innovations | Aurora Estates | Quantum Quest Logistics | EcoSphere Ventures | Mystic Brews Café"
    "Correct Selection: Company.name"]

   ["Inputs: alex.jordan23@example.com | creative.minds88@inbox.com | thewanderer91@journey.net | tech.titan4@innovate.io | starlight.muse@artmail.com"
    "Correct Selection: Internet.emailAddress"]

   ["Inputs: Blitz | Mariners | Sirens | Vanguards | Crusaders"
    "Correct Selection: Team.name"]])

(def head
  ["<s>[INST]"
   "Task:"
   "1. Given a set of inputs, identify what kind of data it is from the provided list below."
   "2. Output your selection. Give NO explanation for it. Give ONLY your selection."
   ""
   "List of items: Address.countyByZipCode, Address.fullAddress, Address.cityName, Address.citySuffix, Address.zipCodeByState, Address.streetSuffix, Address.streetPrefix, Address.latitude, Address.timeZone, Address.stateAbbr, Address.streetName, Address.buildingNumber, Address.firstName, Address.state, Address.zipCode, Address.streetAddress, Address.streetAddressNumber, Address.countryCode, Address.country, Address.lastName, Address.city, Address.longitude, Address.secondaryAddress, Address.cityPrefix, Ancient.primordial, Ancient.god, Ancient.hero, Ancient.titan, Animal.name, App.version, App.name, App.author, AquaTeenHungerForce.character, Artist.name, Avatar.image, Aviation.aircraft, Aviation.METAR, Aviation.airport, BackToTheFuture.character, BackToTheFuture.date, BackToTheFuture.quote, Beer.style, Beer.name, Beer.malt, Beer.hop, Beer.yeast, Book.genre, Book.title, Book.author, Book.publisher, Bool.bool, Buffy.celebrities, Buffy.episodes, Buffy.quotes, Buffy.bigBads, Buffy.characters, Business.creditCardNumber, Business.creditCardType, Business.creditCardExpiry, Cat.registry, Cat.name, Cat.breed, ChuckNorris.fact, Code.imei, Code.ean8, Code.gtin8, Code.isbnGs1, Code.isbn10, Code.asin, Code.ean13, Code.gtin13, Code.isbnRegistrant, Code.isbn13, Code.isbnGroup, Color.name, Color.hex, Commerce.promotionCode, Commerce.material, Commerce.department, Commerce.price, Commerce.productName, Commerce.color, Company.profession, Company.industry, Company.catchPhrase, Company.buzzword, Company.suffix, Company.logo, Company.name, Company.bs, Company.url, Country.capital, Country.countryCode2, Country.name, Country.flag, Country.countryCode3, Country.currencyCode, Country.currency, Crypto.md5, Crypto.sha256, Crypto.sha1, Crypto.sha512, Currency.code, Currency.name, Date.birthday, Date.future, Date.between, Date.past, Demographic.maritalStatus, Demographic.race, Demographic.demonym, Demographic.sex, Demographic.educationalAttainment, Dog.memePhrase, Dog.breed, Dog.name, Dog.sound, Dog.age, Dog.coatLength, Dog.gender, Dog.size, DragonBall.character, Dune.saying, Dune.character, Dune.planet, Dune.title, Dune.quote, Educator.university, Educator.campus, Educator.secondarySchool, Educator.course, ElderScrolls.quote, ElderScrolls.city, ElderScrolls.lastName, ElderScrolls.region, ElderScrolls.firstName, ElderScrolls.dragon, ElderScrolls.creature, ElderScrolls.race, Esports.game, Esports.league, Esports.event, Esports.player, Esports.team, File.fileName, File.mimeType, File.extension, Finance.bic, Finance.iban, Finance.creditCard, Food.dish, Food.spice, Food.ingredient, Food.vegetable, Food.measurement, Food.fruit, Food.sushi, Friends.location, Friends.character, Friends.quote, FunnyName.name, GameOfThrones.quote, GameOfThrones.city, GameOfThrones.character, GameOfThrones.house, GameOfThrones.dragon, Hacker.adjective, Hacker.noun, Hacker.verb, Hacker.ingverb, Hacker.abbreviation, HarryPotter.location, HarryPotter.book, HarryPotter.house, HarryPotter.character, HarryPotter.spell, HarryPotter.quote, Hipster.word, HitchhikersGuideToTheGalaxy.location, HitchhikersGuideToTheGalaxy.planet, HitchhikersGuideToTheGalaxy.specie, HitchhikersGuideToTheGalaxy.marvinQuote, HitchhikersGuideToTheGalaxy.quote, HitchhikersGuideToTheGalaxy.starship, HitchhikersGuideToTheGalaxy.character, Hobbit.thorinsCompany, Hobbit.location, Hobbit.quote, Hobbit.character, HowIMetYourMother.catchPhrase, HowIMetYourMother.quote, HowIMetYourMother.character, HowIMetYourMother.highFive, IdNumber.valid, IdNumber.invalid, IdNumber.invalidSvSeSsn, IdNumber.ssnValid, IdNumber.validSvSeSsn, Internet.domainSuffix, Internet.image, Internet.avatar, Internet.ipV4Cidr, Internet.publicIpV4Address, Internet.domainName, Internet.password, Internet.ipV6Cidr, Internet.userAgentAny, Internet.userAgent, Internet.emailAddress, Internet.safeEmailAddress, Internet.macAddress, Internet.privateIpV4Address, Internet.ipV6Address, Internet.url, Internet.ipV4Address, Internet.domainWord, Internet.uuid, Internet.slug, Job.seniority, Job.field, Job.title, Job.keySkills, Job.position, LeagueOfLegends.location, LeagueOfLegends.champion, LeagueOfLegends.masteries, LeagueOfLegends.quote, LeagueOfLegends.rank, LeagueOfLegends.summonerSpell, Lebowski.character, Lebowski.actor, Lebowski.quote, LordOfTheRings.character, LordOfTheRings.location, Lorem.sentence, Lorem.sentences, Lorem.character, Lorem.characters, Lorem.paragraphs, Lorem.paragraph, Lorem.fixedString, Lorem.word, Lorem.words, Matz.quote, Medical.hospitalName, Medical.medicineName, Medical.symptoms, Medical.diseaseName, Music.key, Music.instrument, Music.genre, Music.chord, Name.title, Name.name, Name.suffix, Name.lastName, Name.nameWithMiddle, Name.fullName, Name.firstName, Name.prefix, Name.username, Name.bloodGroup, Nation.flag, Nation.language, Nation.nationality, Nation.capitalCity, Number.randomNumber, Number.randomDouble, Number.digits, Number.digit, Number.randomDigitNotZero, Number.numberBetween, Number.randomDigit, Options.nextElement, Options.option, Overwatch.quote, Overwatch.location, Overwatch.hero, PhoneNumber.extension, PhoneNumber.cellPhone, PhoneNumber.subscriberNumber, PhoneNumber.phoneNumber, Pokemon.location, Pokemon.name, PrincessBride.quote, PrincessBride.character, ProgrammingLanguage.name, ProgrammingLanguage.creator, Relationships.spouse, Relationships.inLaw, Relationships.sibling, Relationships.extended, Relationships.parent, Relationships.any, Relationships.direct, RickAndMorty.quote, RickAndMorty.character, RickAndMorty.location, Robin.quote, RockBand.name, Shakespeare.asYouLikeItQuote, Shakespeare.hamletQuote, Shakespeare.kingRichardIIIQuote, Shakespeare.romeoAndJulietQuote, SlackEmoji.emoji, SlackEmoji.celebration, SlackEmoji.nature, SlackEmoji.objectsAndSymbols, SlackEmoji.foodAndDrink, SlackEmoji.activity, SlackEmoji.custom, SlackEmoji.travelAndPlaces, SlackEmoji.people, Space.nasaSpaceCraft, Space.galaxy, Space.meteorite, Space.star, Space.agencyAbbreviation, Space.constellation, Space.company, Space.nebula, Space.distanceMeasurement, Space.moon, Space.agency, Space.starCluster, Space.planet, StarTrek.location, StarTrek.villain, StarTrek.character, StarTrek.specie, Stock.nyseSymbol, Stock.nsdqSymbol, Superhero.descriptor, Superhero.name, Superhero.power, Superhero.suffix, Superhero.prefix, Team.name, Team.creature, Team.state, Team.sport, TwinPeaks.character, TwinPeaks.location, TwinPeaks.quote, University.suffix, University.name, University.prefix, Weather.temperatureFahrenheit, Weather.temperatureCelsius, Weather.description, Witcher.quote, Witcher.character, Witcher.witcher, Witcher.monster, Witcher.school, Witcher.location, Yoda.quote, Zelda.character, Zelda.game"
   ""
   "Remember, your selection MUST come from the list. You CANNOT output any explanation for your selection."
   ""
   "Examples:"])

(def base-prompt
  (format "%s\n\n%s"
          (s/join "\n" head)
          (s/join "\n\n" (map #(s/join "\n" %) examples))))

(defn stitch-prompt [phrase]
  (format "%s\n\nInputs: %s\nCorrect Selection: [/INST]" base-prompt phrase))

(defn predict [text {:keys [seed url model]}]
  (let [endpoint (format "%s/api/generate" url)
        body {:model model
              :prompt (stitch-prompt text)
              :stream false
              :options {:temperature 0 :seed seed :stop ["\n"]}}
        response (http/post endpoint {:body (json/generate-string body)})]
    (-> response
        (:body)
        (json/parse-string)
        (get "response")
        (s/trim))))

(defmulti to-shadowtraffic
  (fn [spec llm-opts]
    (:kind spec)))

(defn sd [m2 n]
  (if (= n 1) m2 (Math/sqrt (/ m2 (dec n)))))

(defmethod to-shadowtraffic :integer
  [{:keys [mean count m2]} llm-opts]
  (let [sd (sd m2 count)]
    {"_gen" "normalDistribution"
     "mean" mean
     "sd" sd
     "decimals" 0}))

(defmethod to-shadowtraffic :double
  [{:keys [mean count m2]} llm-opts]
  (let [sd (sd m2 count)]
    {"_gen" "normalDistribution"
     "mean" mean
     "sd" sd}))

(defmethod to-shadowtraffic :boolean
  [spec llm-opts]
  {"_gen" "boolean"})

(defmulti override-string
  (fn [spec expr]
    expr))

(defmethod override-string "Internet.uuid"
  [spec expr]
  {"_gen" "uuid"})

(defmethod override-string "Options.option"
  [{:keys [elements]} expr]
  {"_gen" "weightedOneOf"
   "choices" (map (fn [[x weight]] {"weight" weight "value" x}) (frequencies elements))})

(defmethod override-string "Date.future"
  [spec expr]
  {"_gen" "formatDateTime" "ms" {"_gen" "now"}})

(defmethod override-string "Date.between"
  [spec expr]
  {"_gen" "formatDateTime" "ms" {"_gen" "now"}})

(defmethod override-string "Date.past"
  [spec expr]
  {"_gen" "formatDateTime" "ms" {"_gen" "now"}})

(defmethod override-string :default
  [spec expr]
  {"_gen" "string"
   "expr" (format "#{%s}" expr)})

(defmethod to-shadowtraffic :string
  [{:keys [elements] :as spec} llm-opts]
  (let [expr (predict (s/join " | " elements) llm-opts)]
    (override-string spec expr)))

(defmethod to-shadowtraffic :default
  [spec llm-opts]
  {"_gen" "constantly" "x" nil})

(defn integrate-specs [generator path specs llm-opts]
  (if (= (count specs) 1)
    (assoc-in generator path (to-shadowtraffic (first specs) llm-opts))
    (assoc-in generator path {"_gen" "oneOf" "choices" (map (fn [spec] (to-shadowtraffic spec llm-opts)) specs)})))
