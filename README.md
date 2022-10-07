
# Bottom Dialog Android Picker

[![](https://jitpack.io/v/mahdiasd/BottomDialogFilePicker.svg)](https://jitpack.io/#mahdiasd/BottomDialogFilePicker)

Bottom dialog picker like telegram for all version of android (1 ... , 10 , 11 , 12 , 13)

Search in Files

Support android 10+

Expandable and scrollable dialog

Full Customisable (Color , text , minimum and maximum selected file size , ...)

No required runtime permission


## Screenshots

<img src="https://raw.githubusercontent.com/mahdiasd/BottomDialogFilePicker/master/screenshot/1.png" width="200" height="450" /> <img src="https://raw.githubusercontent.com/mahdiasd/BottomDialogFilePicker/master/screenshot/2.png" width="200" height="450" />
<img src="https://raw.githubusercontent.com/mahdiasd/BottomDialogFilePicker/master/screenshot/3.png" width="200" height="450" /><img src="https://raw.githubusercontent.com/mahdiasd/BottomDialogFilePicker/master/screenshot/4.png" width="200" height="450" />
<img src="https://raw.githubusercontent.com/mahdiasd/BottomDialogFilePicker/master/screenshot/5.png" width="200" height="450" />

## Installation

#### Step 1. Add the JitPack repository to your build file

Install my project with gradle
Add it in your root build.gradle at the end of repositories:


```bash
  allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}
```
#### Step 2. Add the dependency

```bash
  dependencies {
	        implementation 'com.github.mahdiasd:BottomDialogFilePicker:1.0.4'
	}
```
## Ho To Use

#### A- Easy Use:

```
 FilePicker(this, supportFragmentManager)
            .setMode(PickerMode.Image, PickerMode.Audio, PickerMode.FILE, PickerMode.Video)
            .setListener(object : FilePickerListener {
                override fun selectedFiles(list: List<FileModel>?) {
                    // your code...
                }
            })
            .show()
```

#### B- Adcanced Use:

```
 FilePicker(context, supportFragmentManager)
            .setMode(PickerMode.Audio, PickerMode.Video, PickerMode.FILE, PickerMode.Image)
            .setDefaultMode(PickerMode.Image)
            .setMaxSelection(5)
            .setMaxEachFileSize(1 * 1000) // mean -> 1 mb 
            .setMaxTotalFileSize(15 * 1000) // mean -> 15 mb
            .setCustomText("video", "audio", "storage", "image", "openStorage")
            .setShowFileWhenClick(true)
            .setCardBackgroundColor(ContextCompat.getColor(context, R.color.white))
            .setDeActiveColor(ContextCompat.getColor(context, R.color.gray))
            .setActiveColor(ContextCompat.getColor(context, R.color.colorPrimary))
            .setIcons(
                videoIcon = ContextCompat.getDrawable(context, R.drawable.ic_video),
                audioIcon = ContextCompat.getDrawable(context, R.drawable.ic_audio),
                imageIcon = ContextCompat.getDrawable(context, R.drawable.ic_image),
                fileManagerIcon = ContextCompat.getDrawable(context, R.drawable.ic_file),
            )
            .setListener(object : FilePickerListener {
                override fun selectedFiles(list: List<FileModel>?) {
                    // your code ...
                }
            })
            .show()
```

#### You can also make some changes through the strings file
```
<string name="mahdiasd_file_picker_failed_open_file">Error when open file, please choose another!</string>

<string name="mahdiasd_file_picker_cant_find_this_file">Can`t find this file</string>

<string name="mahdiasd_file_picker_video">Video</string>

<string name="mahdiasd_file_picker_audio">Audio</string>

<string name="mahdiasd_file_picker_file_manager">File Manager</string>

<string name="mahdiasd_file_picker_image">Image</string>

<string name="mahdiasd_file_picker_open_storage">Open Storage</string>

<string name="mahdiasd_file_picker_max_total_size">Max size for total files is</string>

<string name="mahdiasd_file_picker_max_each_size">Max size for each files is</string>

<string name="mahdiasd_file_picker_search">Search…</string>
```


## This library uses the following libraries

- [Glide](https://github.com/bumptech/glide)
- [Handle Path Oz](https://github.com/onimur/handle-path-oz)

Thanks to the very powerful Glide and Handle Path Oz

## Proguard

```
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep class * extends com.bumptech.glide.module.AppGlideModule {
 <init>(...);
}
-keep public enum com.bumptech.glide.load.ImageHeaderParser$** {
  **[] $VALUES;
  public *;
}
-keep class com.bumptech.glide.load.data.ParcelFileDescriptorRewinder$InternalRewinder {
  *** rewind();
}

# Uncomment for DexGuard only
#-keepresourcexmlelements manifest/application/meta-data@value=GlideModule
```
