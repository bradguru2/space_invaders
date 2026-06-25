## Space Invaders Clone

### Download and run

Java 17 or newer is required. The release distribution includes the application,
its dependencies, native libraries for Windows, Linux, and macOS, and launcher
scripts for each platform.

1. Download `space_invaders-shadow-<version>.zip` from the
   [latest release](https://github.com/bradguru2/space_invaders/releases/latest).
2. Extract the ZIP.
3. Start the game:
   - Windows: `bin\space_invaders.bat`
   - Linux and macOS: `bin/space_invaders`

The locale defaults to English. Set `LOCALE` environment variable to a language tag such as `es`,
`fr`, or `pt-BR` before starting the game to use another available translation.

### Build from source

Build the same distribution ZIP used by GitHub releases:

```shell
./gradlew clean shadowDistZip
```

The archive is written to `build/distributions/`.
It includes `README.html`, a browser-friendly launch guide for non-developers.

### Creating a release

Releases are created automatically when a version tag is pushed. The tag
determines the version embedded in the distribution.

```shell
git tag -a v1.4.0 -m "Release v1.4.0"
git push origin v1.4.0
```

Tags must use a version such as `v1.4.0` or `v1.4.0-rc.1`. The release workflow
builds the cross-platform distribution, creates a SHA-256 checksum, and attaches
both files to a GitHub release.

### How to Play
 - Press F2 to start the game.
 - Move the ship left and right using the keyboard.
 - Press **Spacebar** to fire missiles.
 - Avoid missiles and don't get invaded! 
 
### Toggling Fullscreen
 Fullscreen mode can be toggled using the standard GLFW approach:
 - Press ALT + ENTER to toggle 

Enjoy the game, and feel free to extend it further!

### Cool stuff to mention
#### Generative AI can do language translation

| Locale | Score       | Ships        | Start Game           |
|--------|-------------|--------------|----------------------|
| en     | Score       | Ships        | Start Game           |
| es     | Puntaje     | Barcos       | Iniciar juego        |
| fr     | Skor        | Navires      | Démarrer le jeu      |
| de     | Punktestand | Schiffe      | Spiel starten        |
| it     | Punteggio   | Navi         | Avvia gioco          |
| pt     | Pontuação   | Navios       | Iniciar jogo         |
| pt-BR  | Pontuação   | Navios       | Iniciar jogo         |
| nl     | de Score    | Schepen      | Spel starten         |
| ru     | Счёт        | Корабли      | Начать игру          |
| uk     | Рахунок     | Кораблі      | Почати гру           |
| pl     | Wynik       | Statki       | Rozpocznij grę       |
| cs     | Skóre       | Lodě         | Spustit hru          |
| sk     | Skóre       | Lode         | Spustiť hru          |
| hu     | Pontszám    | Hajók        | Játék indítása       |
| ro     | Scor        | Nave         | Pornește jocul       |
| bg     | Резултат    | Кораби       | Стартирай играта     |
| sr     | Резултат    | Бродови      | Pokreni igru         |
| hr     | Rezultat    | Brodovi      | Pokreni igru         |
| bs     | Rezultat    | Brodovi      | Pokreni igru         |
| mk     | Резултат    | Бродови      | Започни игра         |
| el     | Σκορ        | Πλοία        | Έναρξη παιχνιδιού    |
| tr     | Skor        | Gemiler      | Oyunu başlat         |
| sv     | Poäng       | Skepp        | Starta spelet        |
| da     | Point       | Skibe        | Start spil           |
| no     | Poeng       | Skip         | Start spill          |
| fi     | Pisteet     | Laivat       | Aloita peli          |
| is     | Stig        | Skip         | Byrja leik           |
| lv     | Rezultāts   | Kuģi         | Sākt spēli           |
| lt     | Rezultatas  | Laivai       | Pradėti žaidimą      |
| et     | Skoor       | Laevad       | Alusta mängu         |
| ja     | スコア         | シップ          | ゲームを開始               |
| zh-CN  | 得分          | 船只           | 开始游戏                 |
| zh-TW  | 得分          | 船隻           | 開始遊戲                 |
| ko     | 점수          | 배들           | 게임 시작                |
| vi     | Điểm        | Tàu          | Bắt đầu trò chơi     |
| th     | คะแนน       | เรือ         | เริ่มเกม             |
| id     | Skor        | Kapal        | Mulai permainan      |
| ms     | Skor        | Kapal-Kapal  | Mula permainan       |
| fil    | Iskor       | Mga barko    | Simulan ang laro     |
| hi     | स्कोर       | जहाज़        | खेल शुरू करें        |
| bn     | স্কোর       | জাহাজ        | খেলা শুরু করুন       |
| ta     | மதிப்பெண்   | கப்பல்கள்    | விளையாட்டை தொடங்கு   |
| te     | స్కోరు      | నౌకలు        | ఆట ప్రారంభించు       |
| ml     | സ്കോർ       | കപ്പലുകൾ     | കളി ആരംഭിക്കുക       |
| kn     | ಅಂಕ         | ಹಡಗುಗಳು      | ಆಟ ಪ್ರಾರಂಭಿಸಿ        |
| mr     | गुण         | जहाजे        | खेळ सुरू करा         |
| gu     | સ્કોર       | જહાજો        | રમત શરૂ કરો          |
| pa     | ਸਕੋਰ        | ਜਹਾਜ਼        | ਖੇਡ ਸ਼ੁਰੂ ਕਰੋ        |
| ur     | اسکور       | جہاز         | کھیل شروع کریں       |
| fa     | امتیاز      | کشتی‌ها      | بازی را شروع کنید    |
| ar     | النتيجة     | السفن        | ابدأ اللعبة          |
| he     | ניקוד       | ספינות       | התחלת המשחק          |
| am     | ነጥብ         | መርከቦች        | የጨዋታ መጀመሪያ           |
| sw     | Alama       | Meli         | Anza mchezo          |
| zu     | Amanqaku    | Izikebhe     | Qala umdlalo         |
| af     | Telling     | Skepe        | Begin spel           |
| xh     | Amanqaku    | Iinqanawa    | Qala umdlalo         |
| yo     | Ami         | Ọkọ̀ ojú omi | Bẹrẹ ere             |
| ha     | Maki        | Jiragen ruwa | Fara wasa            |
| km     | ពិន្ទុ      | កប៉ាល់       | ចាប់ផ្តើមហ្គេម       |
| my     | အမှတ်       | သင်္ဘောများ  | ဂိမ်းစတင်ပါ          |
| ne     | स्कोर       | जहाजहरू      | खेल शुरू गर्नुहोस्   |
| si     | ලකුණු       | නැව්         | ක්‍රීඩාව ආරම්භ කරන්න |
| kk     | Ұпай        | Кемелер      | Ойынды бастау        |
| uz     | Hisob       | Kemalar      | O'yinni boshlash     |
| az     | Hesab       | Gəmilər      | Oyuna başla          |
| ka     | ქულა        | ხომალდები    | თამაშის დაწყება      |
| hy     | Միավոր      | Նավեր        | Խաղը սկսել           |
| ti     | ነጥብ         | መርከቦች        | ጨዋታ መጀመሪያ            |
| tl     | Iskor       | Mga barko    | Simulan ang laro     |

#### AI Assist: RetroFont and RetroSynth
While building RetroFont, I used AI to prototype the glyph atlas pipeline and to sanity check the math for UV mapping. It helped me explore tradeoffs in texture size, padding, and sampling so the font stayed crisp without wasting VRAM. The back and forth was most useful when I could paste a small shader or Kotlin snippet and ask for targeted improvements or edge cases.

For RetroSynth, AI was useful for brainstorming a clean architecture that kept the sound engine simple but flexible. I iterated on ADSR envelopes, mixing, and a tiny DSL for arranging notes, and had the model point out corner cases like clipping, buffer underruns, and mismatched sample rates. It also helped me draft test tones and a basic oscillator toolkit that I refined by ear.

Overall, AI acted like a fast design partner for roughing in ideas, then I took over for the final tuning and integration. The best outcomes came from short, specific prompts and validating everything in the actual game loop.

#### Lessons Learned
* Was fairly easy to use AI to generate the table
* Some of the languages have very large "alphabets" so those got trimmed in implementation
  * Have to watch the size of Atlas texture in implementation 
* Some of the languages require more features like special shaping so that those are not 100% in implementation

#### Attribution
Ship, UFO, and Invader images are Atari copyrighted assets used here for non-commercial purposes and no profit is intended.
Soundtrack was provided courtesy of https://pixabay.com/users/nickpanek-38266323/
