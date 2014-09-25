<b>Note: </b> Changelogs will no longer be updated in this file. Check the individual commit comments for all further update descriptions.

Beta 0.9.1 (12/20/2013)
-------------------------

* Fixed crashes when trying to rebuild music library.
* Pinning songs now checks for local audio files and skips them.
* Fixed issue where editing album/artist/song tags with special chars would crash the app.
* Fixed issue where editing album tags with special chars wouldn't update the database.
* Removed kinks with retrieving/deleting album art.
* Removed thumb for seekbar.
* Fixed playback kinks with widgets.
* Large Widget now uses the UniversalImageLoader library to display ListView album art.
* Fixed issue where Large Widget wouldn't refresh current queue.
* Tapping on a widget's album art opens up NowPlayingActivity.
* Fixed Issue #63618 with KitKat. See https://code.google.com/p/android/issues/detail?id=63618
* Slimmed down borders in FoldersFragment's list view.
* Fixed Enqueue issues where only the first song would be enqueued after a fresh enqueue operation.
* The app's main logo is now used as the default album art.
* Fixed ArtistsFlippedFragment's header image.
* Fixed AlbumArtistsFlippedFragment's header image.
* Fixed issue where album art wouldn't disappear after deleting it from ArtistsFlippedFragment.
* All Access songs are now excluded when building the music library.
* Simplifed Google Play Music options in Settings.

Beta 0.9.2 (12/21/2013)
-------------------------

* Removed streaming progressbar for local audio files.
* Empty fields for GMusic songs are now repalced with "Unknown xxx".
* Fixed NoSuchMethod exceptions (.setDrawable() was added in API 16).
* initializeDatabaseCursor() is now executed asynchronously.
* Added dummy UI elements to NowPlayingActivity to disguise the execution of initializeDatabaseCursor().
* Fixed lag issue when scrolling quickly through songs in NowPlayingActivity.
* Widget updates are now executed asynchronously.
* Notification updates are now executed asynchronously.
* Fixed issue where controlling playback from the notification wouldn't update controls in NowPlayingActivity.

Beta 0.9.3 (12/22/2013)
-------------------------

* FLAC support (with album art).
* Support for fetching folder art.
* Album art/track info scrolling is now animated.
* Redesigned folder playback content/logic.
* Playing songs from folder view is now light years faster.
* Fixed issues where the wrong track would sometimes be displayed on the player screen.
* Fixed issues where the wrong track would sometimes be displayed on the current queue screen.
* Fixed issues with enqueuing folder tracks and regular library tracks.
* Fixed issues with headset buttons.

Beta 0.9.4 (12/25/2013)
-------------------------

* Fixed crashes when the current queue is accessed while the cursor is still being built (folder view).
* Fixed seekbar crashes.
* Added trial time limit (7 days).
* Integrated Google Play In-App Billing to unlock the full version ($2.99).
* Fixed Notification loophole for the 7 day trial.
* Added open source licenses.
* Added Upgrade option to Settings screen.
* Fixed contact button.
* Added "how-to" overlays to the player screen.
* Upgraded the "how-to" overlay for the main screen.
* The trial version dialog can no longer be closed by tapping outside the dialog.
* Fixed issue where All Access tracks were being included in music library.
* Pinning songs now takes place on a background service instead of a killable AsyncTask.
* Added option to migrate pinned songs from the official Google Play Music app.
* Fixed issue with enqueuing a song when no music is playing.
* Blocked tag editing for Google Play Music songs (will be added in a future update).
* Added equalizer toggle in Settings.
* Fixed issue where player screen wouldn't update if a GMusic song failed to load.
* Updated ActionBar theme.
* Holo Card themes have a slick, new look.
* Minor bug fixes when playing from Folder view.
* Hitting the back button goes back to the parent directory in Folder view.
* Lyrics are now larger and more legible.
* Fixed "Reset Blacklist" option in Settings.

Release 1.0 (12/27/2013)
-------------------------

* Added scrobbling support via Simple LastFM Scrobbler and ScrobbleDroid.
* Added lockscreen controls/fixed AudioFocus issues.
* Fixed issue where the wrong widget was being loaded.
* Fixed equalizer issues.
* The trial dialog is only shown every 5 startups now.

Release 1.1 (12/28/2013)
-------------------------

* Current song in queue is now highlighted.
* Fixed issue with shuffle.
* Added two new track change animations. You can configure them in Settings.
* Added option to set default (starting) folder in Folders view.
* Fixed issues where .midi and .m4a weren't being added to the library.
* Notification is no longer cut off when collapsed.

Release 1.2 (12/28/2013)
-------------------------

* Temporary KitKat scrollbar fix.
* Fixed Welcome screen crash (when picking folders).
* Fixed crash when backing out of a playlist.
* Fixed back button crash when the user is already in the root directory (/).
* Fixed player crash when playing from Folders view.
* Fixed issue where app opens up Chrome on startup.
* Fixed issue where some ID3 tags weren't being read properly.
* Fixed HTC One volume control issue.
* Updated scan process UI. App can now be closed during scan.
* Restructured Album Artists browser: Album Artists > Albums > Songs.
* Fixed random track skips when streaming from Google Play Music.
* Fixed playlist pinning.

Release 1.3 (12/29/2013)
-------------------------

* Added new "On This Device" library. 
* Fixed library scan crashes.
* Fixed playlist CRUD operation Toast messages.
* Fixed issue with playlist names with apostrophes.
* Play all/Enqueuing an artist, album artist or a genre now sorts by album and then track number.
* Restructed Genres: Genres > Albums in Genre > Songs in Album in Genre
* Fixed 00:00 issue in Artists view.
* Fixed crash when picking a music folder.
* Fixed crash when trying to get all pinned songs from official GMusic app.
* Fixed small widget.

Release 1.3.1 (12/30/2013)
-------------------------

* Fixed crash when trying to play a song (4.0.x devices).

Release 1.3.2 (12/30/2013)
-------------------------

* Fixed issue where music playback would start again after a notification came in.
* Fixed widget play/pause button issues.
* Fixed small widget's issue where tapping album wouldn't open up the app.

Release 1.4 (12/30/2013)
-------------------------

* New "Show more by/in this artist/album artist/album/genre" in player screen.
* Fixed crashes in Folders view.
* Fixed equalizer issue.
* Switched to official Google navigation drawer.
* Improved overal app performance.
* New layout for inner browsers.

Release 1.4.1 (12/31/2013)
-------------------------

* Bug fixes.
* Fixed v1.4 startup crash.
* Playlists from other music players can now be imported (Settings > Rebuild Music Library).

Release 1.4.2 (12/31/2013)
-------------------------

* Added option to select default startup screen (Settings > Default Startup Screen).
* Added option to pick between a grid or a list for artist, album artists, and albums.
* Minor bug fixes.

Release 1.5 (1/2/2014)
-------------------------

* New layout for tablets and phones.
* Transitioned from context menus to popup menus.
* Shuffle glitches fixed.
* Performance enhancements.
* Fixed background color issues with "Light Theme" and "Dark Theme".
* Fixed bugs that were reported.

Release 1.5.1 (1/3/2014)
-------------------------

* Fixed crash when batch editing album tags.
* Improved tag editing performance.
* Added more widget themes.
* Trial engine redesigned.
* Library building process is more stable.

Release 1.6 (1/6/2014)
-------------------------

* Local playlists are now synced with Android's MediaStore database.
* Added option to edit genre in tag editor dialogs.
* Bug fixes.

Release 1.7 (1/12/2014)
-------------------------

* Added new "Play Next" feature.
* "Show lyrics" option added to player screen overflow menu.
* Bug fixes.
* Minor visual tweaks.

Release 2.0 (1/20/2014)
-------------------------

* NEW! Save current position for any track. Useful for audiobooks/podcasts (Player overflow menu > Save current position/Clear saved position).
* NEW! DashClock extension added.
* NEW! Option to toggle lockscreen player controls (Settings > Lockscreen Controls).
* Folder view items (files and folders) are now sorted by name.
* Fixed issues with lockscreen controls not updating.
* Fixed issues with current queue not updating after resuming player screen.
* Other miscellaneous bug fixes.

Release 2.0.1 (1/21/2014)
-------------------------

* Fixed a couple of crashes when trying to play songs.
* Fixed issue where some songs' album art wouldn't be detected/displayed.
* Fixed issues with songs not playing when they have apostrophes in their file names.
* Other bug fixes with a whole bunch of technical lingo, so I'll leave out the details.

Release 2.0.2 (1/22/2014)
-------------------------

* Minor UI tweaks.
* Bug fixes for crash reports that were sent to me in the past 1-2 days.

Release 2.0.3 (1/23/2014)
-------------------------

* Hidden files are no longer included during scanning process.
* Minor UI tweaks.
* Bug fixes.

Release 2.1 (1/24/2014)
-------------------------

* New player screen layout with access to current queue.
* New "Color Accent" option in Settings. Replaces old "Player Color Scheme" option.
* Equalizer now disabled by default for all HTC devices, due to known Android bug. You can re-enable the equalizer by going to Settings.
* Better organization for player screen overflow menu.
* Better touch feedback on player screen controls.
* Bug fixes.

Release 2.1.1 (1/26/2014)
-------------------------

* NEW! Share Jams with your friends on Facebook and you could get 50% off your upgrade price! Check app for more details.
* Updating the color accent reflects the new changes across the whole app.
* Fixed folder view crash.
* Fixed equalizer issue with bass boost/virtualizer.

Release 2.1.2 (1/28/2014)
-------------------------

* Integrated Google Analytics.
* Updated Facebook promo: Everyone gets 30% off when they share the app with their Facebook friends. No more lottery system.
* Fixed issues with songs not playing if they have weird characters in their file names (#, %, ^, &, @, etc).
* Moah bug fixes.

Release 2.1.3 (1/29/2014)
-------------------------

* Added service tracking for GAnalytics.

Release 2.1.2 (1/29/2014)
-------------------------

* Added campaign tracking for GAnalytics (Facebook, 30% off campaign).

Release 2.2 (2/23/2014)
-------------------------

* Removed Now Playing footer and added playback controls to current queue drawer.
* Transparent navigation bar and status bar for KitKat users (Android 4.4+).
* Improved/fixed music library scanning process.
* Playback volume lowered (ducked) for driving directions, notifications, etc.
* Added option to adjust crossfade duration.
* Added "Play All" and "Shuffle All" options to main screen overflow menu.
* Equalizer no longer locked to landscape mode.
* Bug fixes.