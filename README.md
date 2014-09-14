Twik
============

Twik is an easy to use password generator and manager for Android. Your passwords are always available when you need them, but they are never stored anywhere!

Keeping up with todays password requirements isn’t easy. On the one hand, you want to have different, hard-to-guess passwords for each website and service. On the other hand, it’s hard to remember so many passwords! You can rely on a web service to keep all your passwords, and make them readily available from any device. However, this creates a single point of failure: if this password service is compromised, all your passwords would be leaked!

Twik works in a different way. You have to provide a private key that will be stored in the device, and think of a strong master key that will not be stored anywhere. Any time you need to generate a password for a website, you must type your master key. The combination of the master key, the private key, and the website will be used to generate a unique, strong password. Since passwords are generated each time, even if one of them is compromised the rest would be safe. Twik also integrates with any web browser, so that you can generate a password quickly by sharing a website with Twik from the browser.

Twik is compatible with Password Hasher Plus, a Chrome extension by Eric Woodruff that follows the same principles for generating strong passwords. You can use the same private and master keys to generate the same passwords on your desktop browser.

Twik features include:
- Several profiles, each with its own private key
- Favicons to easily identify websites
- Identicons to check that you typed your master key correctly at a glance
- Share any website from a web browser to generate a password for it
- Customize password generation for each website (password length and characters)
- Automatically copy generated passwords to the clipboard

Building
--------

The source code includes an Android Studio project. Just open the project and 
build it to generate the apk file, which can be installed on the device.

Acknowledgments
---------------

French translation by Andrés Álvarez and greizgh.

License
-------

Twik is free software and is distributed under the GPLv3 license. See
COPYING for more information.

External code
-------------

This project uses code from third-parties, licensed under their own terms:
- [Password Hasher](https://addons.mozilla.org/en-US/firefox/addon/password-hasher/)
by Steve Cooper. Licensed under MPL 1.1/GPL 2.0/LGPL 2.1 licenses.
- [Password Hasher Plus](http://passwordhasherplus.com) by Eric Woodruff.
Licensed under MPL 1.1/GPL 2.0/LGPL 2.1 licenses.
- [Contact Identicons](https://github.com/davidhampgonsalves/Contact-Identicons/) by David Hamp-Gonsalves.
- [Android Action Bar Style Generator](http://jgilfelt.github.io/android-actionbarstylegenerator)
by The Android Open Source Project and y readyState Software Ltd.
Licensed under the Apache 2.0 License.
- [Ubuntu Mono](http://font.ubuntu.com) by Canonical Ltd.
Licensed under Ubuntu Font Licence 1.0.
