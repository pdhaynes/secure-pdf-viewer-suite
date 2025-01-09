### secure-pdf-viewer-suite
This is custom tailored for security measures upheld by the Android App SecureStash. This will most likely not serve any general purpose better than the standard libraries altered by this one.


Note that this package sacrifices performance and memory usage for safety.

Overview
-------
This library alters the following classes:
- num42's [Subsampling PDF Decoder](https://github.com/num42/subsampling-pdf-decoder)
  - PDFRegionDecoder -> MemoryPDFRegionDecoder
  - PDFDecoder -> MemoryPDFDecoder
- Lindner Stefan's [PDF Pager Adapter](https://gist.github.com/stefanplindner/ef96abb00204b8e6e0f378cfe026a6d7) 
  - PDFPagerAdapter -> MemoryPDFPagerAdapter

By default, this library uses the following PDF improvement libraries (and you should too):
- Dave Morrissey's [Subscampling Scale Image View](https://github.com/davemorrissey/subsampling-scale-image-view)
- Castor Flex's [Vertical View Pager](https://github.com/castorflex/VerticalViewPager)

Usage
-------
In the Secure Stash app, all user-uploaded files are secured using AES Encryption in the app's local directory, and decoded on the fly within the app. At no point in time should a file ever be in the app's local directory in its unencrypted state. When the user accesses an encrypted file, it will be decrypted and shown to them without using the file on disk as a medium.

This is where this alteration comes in. By modifying the classes to use memory files instead of normal files, we can use the MemoryFile as a medium to view the PDF's instead of a File.

The classes provided in this library need version 21 (Lollipop) or above.

```kotlin
        TODO()
```

For a more complex example see the sample application. There we use a [VerticalViewPager] and a PagerAdapter.
Within the PagerAdapter we use the subsampling-scale-image-view and this's decoder classes.


Download
-------
```groovy
repositories {
    jcenter()
}

dependencies {
    compile 'de.number42:subsampling-pdf-decoder:0.1.0@aar'
}
```

License
-------

    Copyright 2016 Number42

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.


[subsampling-scale-image-view]: https://github.com/davemorrissey/subsampling-scale-image-view
[VerticalViewPager]: https://github.com/castorflex/VerticalViewPager
