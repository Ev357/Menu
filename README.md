![Menu](app/src/main/ic_launcher-playstore.png)

# Menu
Menu is a Wear OS app for seeing, ordering school canteen food using [iCanteen](https://www.nasejidelna.cz).

## Features
- View food items, including allergens and order timing.
- Place food orders (requires logging in).
- Works offline as well.
- Customizable server (defaults to `https://jidelnicek.roznovskastredni.cz`).
- Choose which food types you want to see.

## Installation
1. Download the APK from the latest [release](https://github.com/Ev357/Menu/releases).
2. Install an app that allows sideloading the APK to your watch. I recommend using [GeminiMan WearOS Manager](https://play.google.com/store/apps/details?id=com.geminiman.wearosmanager).
3. I recommend following the the in-app guide. You can find it under the `Help` button.

![Screenshot_2024-04-18-20-36-31-158_com geminiman wearosmanager](https://github.com/Ev357/Menu/assets/113623019/b25d28d2-3adf-4005-bd76-f4c41b808b52)

5. Additionally, I suggest enabling `Disable Automatic Wi-Fi` in the `Developer options` on your watch to prevent interruptions during installation. Remember to disable this setting after installation, as it may cause increased battery drain.

![Screenshot_20240418_205513](https://github.com/Ev357/Menu/assets/113623019/70dacee6-cd83-4004-8ce5-4244d3171382)

6. Connect the app to your watch, then select the APK file you downloaded earlier and click "INSTALL APK FILE".
7. Enjoy the app :DD

_There are sure other ways to do so, but i think this one is the most user friendly one. Also the app is dope XD._

## Examples
![Screenshot_20240418_193706](https://github.com/Ev357/Menu/assets/113623019/4203a51c-796e-402c-9fb6-6cd36c7738b4) ![Screenshot_20240418_193748](https://github.com/Ev357/Menu/assets/113623019/52a956c4-5e90-44e8-9d51-cf9e6af00558)
![Screenshot_20240418_193803](https://github.com/Ev357/Menu/assets/113623019/3a2c5592-ac96-4ffd-81cd-84907f5298d0) ![Screenshot_20240418_193812](https://github.com/Ev357/Menu/assets/113623019/5dd9b357-158c-4c02-9f19-e69581d6b276)
![Screenshot_20240418_194320](https://github.com/Ev357/Menu/assets/113623019/d925a8f3-497c-4266-8eaf-32ee59ea7919) ![Screenshot_20240418_193853](https://github.com/Ev357/Menu/assets/113623019/74fe2be1-a67c-499a-ace2-4025e29f18fa)

## Notes
- Regarding the functionality that's been logged: I've made efforts to secure it, but please be aware that this is my first Kotlin project. While I've tried to ensure security, I can't guarantee it's completely foolproof. Please proceed at your own discretion. However, based on my understanding, it should be satisfactory.
- As mentioned earlier, this is my inaugural project, and I acknowledge that the code isn't optimal. I'm open to suggestions for improvement or learning opportunities.
- Additionally, while logged in, I recommend waiting for everything to to load before doing any orders. There might be some bugs, especially on devices with low performance or slow network speeds, although it should generally function without issue.
- The app functions by simulating a browser and extracting data from HTML, including the logging process. Unfortunately, the APIs used are not publicly accessible.
- This app currently functions on the default server. I cannot guarantee it will work flawlessly for all canteens. If you encounter any bugs, please feel free to report them.
