JFxBorderlessNative
================

This library provide true support of Windows 10 Aero Snap On JavaFx Undecorated non transparent Borderless window
Native code is based on example found on this repository https://github.com/melak47/BorderlessWindow

## Inspiration 
https://github.com/goxr3plus/FX-BorderlessScene
https://github.com/CatWithAWand/BorderlessSceneFX

## Demo
[![Demo]({image-url})]({./demo/demo.webp} "Demo")

## Requirements
Tested with Java 17+ and Windows 11 (may not work on older Windows for now Java8 and old Windows support coming)

## Quick sample

    public BorderlessNative activateSnap(Stage primaryStage,Node moveNode,Node maximizeNode) {
        BorderlessNative borderlessNative= new BorderlessNative(primaryStage);
        borderlessNative.setCaptionNode(moveNode);
        borderlessNative.setMaximizeNode(maximizeNode);
        borderlessNative.makeWindowsBorderless("Sample");
        return borderlessNative;
    }

To run the sample clone this repository and open it from intellij and run the sample
java -Djava.library.path=<yourPath>\AeroBorderless\libs fr.pilli.Sample

## Visual Studio Params
Configuration Properties
Configuration Type = DynamicLibrary .dll
Windows SDK Version = 10.0 (may be downgraded to support Windows 10 or below)
Platform Toolset = Visual Studio 2019 (v142)

For Java 17

C/C++ -> General -> Additional Include Directories C:\PathToJRE\zulu17.40.19-ca-fx-jdk17.0.6-win_x64\include; C:\PathToJRE\JRE\zulu17.40.19-ca-fx-jdk17.0.6-win_x64\include\win32


For Java 8

C/C++ -> General -> Additional Include Directories C:\PathToJRE\zulu8.70.0.23-ca-fx-jdk8.0.372-win_x64\include; C:\PathToJRE\JRE\zulu8.70.0.23-ca-fx-jdk8.0.372-win_x64\include\win32

## TODO Support
Clean sln solution
Support JDK8 and JFX8 OK 
Test on Windows 7 and 10
