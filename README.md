# VIP Reminder ( Capstone Project )

Created as part of Udacity [Android Developer Nanodegree Program](https://eg.udacity.com/course/android-developer-nanodegree-by-google--nd801).

<img src="https://github.com/MohamedAlaaEldin636/VIP-Reminder-Capstone-Project/blob/master/app/src/main/res/mipmap-xxxhdpi/ic_app_launcher_circle.png" />

## App Notes ( Setup and info )

1. App works from installRelease gradle task and keystore file is inside ( /keyStore ) directory
  And the Alias name and password can be found in build.gradle of the app level, just before hitting installRelease
  in build.gradle app level change path of keystore to the same location but in your PC the {$rootDir}/keyStore/myKeyStore.jks
  
2. TODOs 
  * put your google API Key in manifest 
  
3. Todo Notes in code beside comments. ( Notes for the reviewer )

4. Big Note -> Reminders are divided into five categories ( for more info look at the next lines )

  * to get task as ( today ), set up time in current day greater than current time.
  
  * to get task as ( tomorrow ), set up time to tomorrow and you will get it.
  
  * to get task as ( Upcoming ), same as above 2 but with time greater than tomorrow.
  
  * to get task as ( done ) either mark as done from item like-overflow menu while repeat is once or when time comes as well.
  
  * to get task as ( overdue ) go to history tab and mark any task that it's time passed as undone from the item like-overflow menu.

## Libraries / Tech Used
* [MVVM Design Pattern](https://github.com/googlesamples/android-architecture-components/tree/master/BasicSample/#readme)
* [Data Binding](https://developer.android.com/topic/libraries/data-binding)
* [Architecture Components LiveData and ViewModel (with Java 8 Support)](https://developer.android.com/topic/libraries/architecture/adding-components)
* [Room](https://developer.android.com/topic/libraries/architecture/adding-components#room)
* [WorkManager (with JobDispatcher support)](https://developer.android.com/topic/libraries/architecture/workmanager)
* [Timber](https://github.com/JakeWharton/timber)
* [Google Places AND Google Locations](https://developers.google.com/android/guides/setup)
* [Some of Support Library Package](https://developer.android.com/topic/libraries/support-library/packages)

## Screenshots

<img src="https://github.com/MohamedAlaaEldin636/VIP-Reminder-Capstone-Project/blob/master/forReadMeFiles/main_activity_loading.png" height="500" width="300"/> <img src="https://github.com/MohamedAlaaEldin636/VIP-Reminder-Capstone-Project/blob/master/forReadMeFiles/main_activity_without_data.png" height="500" width="300"/> <img src="https://github.com/MohamedAlaaEldin636/VIP-Reminder-Capstone-Project/blob/master/forReadMeFiles/main_activity_with_data.png" height="500" width="300"/>

<img src="https://github.com/MohamedAlaaEldin636/VIP-Reminder-Capstone-Project/blob/master/forReadMeFiles/no_favourite_found.png" height="500" width="300"/> <img src="https://github.com/MohamedAlaaEldin636/VIP-Reminder-Capstone-Project/blob/master/forReadMeFiles/no_history_found.png" height="500" width="300"/>

<img src="https://github.com/MohamedAlaaEldin636/VIP-Reminder-Capstone-Project/blob/master/forReadMeFiles/add_reminder_once.png" height="500" width="300"/> <img src="https://github.com/MohamedAlaaEldin636/VIP-Reminder-Capstone-Project/blob/master/forReadMeFiles/add_reminder_daily.png" height="500" width="300"/>

<img src="https://github.com/MohamedAlaaEldin636/VIP-Reminder-Capstone-Project/blob/master/forReadMeFiles/2_types_of_widgets_in_widget_selection.png" height="500" width="300"/> <img src="https://github.com/MohamedAlaaEldin636/VIP-Reminder-Capstone-Project/blob/master/forReadMeFiles/2_types_of_widgets_and_widget_list_without_data.png" height="500" width="300"/>

<img src="https://github.com/MohamedAlaaEldin636/VIP-Reminder-Capstone-Project/blob/master/forReadMeFiles/widget_list_with_data.png" height="500" width="300"/> <img src="https://github.com/MohamedAlaaEldin636/VIP-Reminder-Capstone-Project/blob/master/forReadMeFiles/widget_list_configuration_activity.png" height="500" width="300"/>

# License

### Apache License Version 2.0, January 2004 http://www.apache.org/licenses/

```
Copyright 2018 Mohamed Alaa

Licensed under the Apache License, Version 2.0 (the "License"); 
you may not use this file except in compliance with the License. 

You may obtain a copy of the License at
http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, 
software distributed under the License is distributed on an "AS IS" BASIS, 
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
See the License for the specific language governing permissions and limitations under the License.
```

| Permissions         | Limitations           | Conditions   |
| ------------------- | --------------------- | ----------- |
| <img src="https://github.com/MohamedAlaaEldin636/VIP-Reminder-Capstone-Project/blob/master/forReadMeFiles/enable_use_icon.png" height="15" width="15"/> Commercial Use         | <img src="https://github.com/MohamedAlaaEldin636/VIP-Reminder-Capstone-Project/blob/master/forReadMeFiles/disable_use_icon.png" height="15" width="15"/> Trademark use | <img src="https://github.com/MohamedAlaaEldin636/VIP-Reminder-Capstone-Project/blob/master/forReadMeFiles/warning_icon.png" height="15" width="15"/> License and copyright notice |
| <img src="https://github.com/MohamedAlaaEldin636/VIP-Reminder-Capstone-Project/blob/master/forReadMeFiles/enable_use_icon.png" height="15" width="15"/> Modification           | <img src="https://github.com/MohamedAlaaEldin636/VIP-Reminder-Capstone-Project/blob/master/forReadMeFiles/disable_use_icon.png" height="15" width="15"/> Liability     |   <img src="https://github.com/MohamedAlaaEldin636/VIP-Reminder-Capstone-Project/blob/master/forReadMeFiles/warning_icon.png" height="15" width="15"/> State changes |
| <img src="https://github.com/MohamedAlaaEldin636/VIP-Reminder-Capstone-Project/blob/master/forReadMeFiles/enable_use_icon.png" height="15" width="15"/> Distribution           | <img src="https://github.com/MohamedAlaaEldin636/VIP-Reminder-Capstone-Project/blob/master/forReadMeFiles/disable_use_icon.png" height="15" width="15"/> Warranty      |    - |
| <img src="https://github.com/MohamedAlaaEldin636/VIP-Reminder-Capstone-Project/blob/master/forReadMeFiles/enable_use_icon.png" height="15" width="15"/> Patent use             | -         |   - |
| <img src="https://github.com/MohamedAlaaEldin636/VIP-Reminder-Capstone-Project/blob/master/forReadMeFiles/enable_use_icon.png" height="15" width="15"/> Private use            | -                 |  - |

