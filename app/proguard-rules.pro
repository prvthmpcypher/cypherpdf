# CypherPDF proguard rules

# PDF Viewer - Preserve all classes from the PDF viewer library
-keep class com.github.barteksc.pdfviewer.** { *; }
-keep class com.github.barteksc.pdfviewer.**$* { *; }

# Keep all public methods in the library
-keepclassmembers class com.github.barteksc.pdfviewer.** {
    public *;
}

# Keep constructors
-keepclasseswithmembernames class com.github.barteksc.pdfviewer.** {
    <init>(...);
}
