[![](https://jitpack.io/v/alperenbabagil/taggy.svg)](https://jitpack.io/#alperenbabagil/taggy)

# Taggy

A tag view library to manage tags for an item. You can set suggested tags or take tag strings from the keyboard directly. It is a highly customizable library in terms of UI.

![](https://user-images.githubusercontent.com/15035624/103465908-09f9fd00-4d51-11eb-964c-65904de77ea3.png)

## Installation
Add it in your root build.gradle at the end of repositories:
```gradle
	allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}
```
Then add these to desired module:
```gradle
implementation "com.github.alperenbabagil:taggy:$version"
```

## Usage
First, add TagView to your layout xml file
```xml
    <com.alperenbabagil.taggylib.TagView
        android:id="@+id/taggyView"
        android:layout_width="300dp"
        android:layout_margin="10dp"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintEnd_toEndOf="parent"
        app:layout_constraintTop_toTopOf="parent"
        android:layout_height="wrap_content"/>
```
Then set suggested and selected tags of item if there are exist.
```kotlin
tagView.apply {
    suggestedTagsLimit=6
    selectedTagsLimit=4
    setSelectedTags(listOf("happy","style","life"))
    setSuggestedTags(listOf("photo","nature","cute","insta","model","music","travel","likesforlike"))
}
```
You can change dimensions and colors by changing values of same keys that library uses. Plus you can change style to modify a component's entire look.


