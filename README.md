# AssetFile

With it, you can operate **Android-asset resources** just like using **java.io.File**.<br/>
Not only the main **AssetFile** class, but also the accompanying **I/O Stream** class and **Reader/Writer** class.<br/>
**Context** and **AssetManager** are automatically obtained and do not require manual transfer of parameters.<br/>

Inspired by https://github.com/xiandanin/AssetFile

JavaDoc: https://tianscar.github.io/AssetFile/

# To get a Git project into your build (gradle):

* Step 1. Add the JitPack repository to your build file<br/>
Add it in your root build.gradle at the end of repositories:<br/>
```
allprojects {
        repositories {
		...
		maven { url 'https://jitpack.io' }
	}
}
```

* Step 2. Add the dependency:<br/>
```
dependencies {
	...
	implementation 'com.github.Tianscar:AssetFile:1.0.4.1'
}
```
