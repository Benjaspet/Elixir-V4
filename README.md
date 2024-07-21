<p align="center">
  <h2 align="center">
      <u>Elixir Music</u>
  </h2>
</p>

<p align="center">
    Elixir Music is an advanced Discord music bot for your server.
</p>

<p align="center">
    <img src="https://img.shields.io/badge/made%20with%20java-blue.svg?logo=data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAADIAAAAyCAYAAAAeP4ixAAAACXBIWXMAAAsTAAALEwEAmpwYAAAE0UlEQVR4nN2ae4hVVRTGt5bWpGVTY2ZRjTpQaKUihNUfTRZmTu8HDT2oICR72R9jRllUUySR+odl9A6rKdSCXlZUFFSGvewNPaeEXkipRFKW84vFfIfW3Z57vXfuubdz++DAmbvPOXt/e++11rfWnhDqDGBc+D8AuATYIzQ6gJuBC0OjA3gPuD80MoDT6MeK0KgAxgHrRWRRaEQAY4Cv+BcnhEYDMAn4wZH4EtghNBKAE4FNjkQfMLPE87sBI0JeAAwCrge2Uohbo+cOAC4HngJ6gWuBISEPAJqA5WwLG+xgPTMdeNm1fQRMDHkBsBewJoXE68BwYCSwKmq7E9gp5AXACGBtCok3gF2BQyKj/wu4LOQJwI7Aqykk3pEB27XO/b4ZmBHyBuDSFBKf21ZS+41RW2fII4C3o4FusCDo2i12JFgb8grg54jIxa5tZ8WPBC+GvIJCT7XFBu/ahgB/u/bfzGZCHgFc4Qb6a0q7uV+P7pBHAM3R9poctbdHRH4HRoc8AjjdDfTRlPbHIjK3hbwCWOyCXWvU1gL86Ih8EfIKYLCthga6JKV9liOyPuQZ9JNZrOgdr8p+jsiq0AgArgSeiH471Lnoqf/l4IYDxwNzgaWS6yuVU0yOMz9LooAp7u8HtFJnlehjAnCHYpO57SWmqrMiYFviceAPtzU2Ahf54Ffk3SQHORW4D2gr8twuJudtpYALoiD7dSaZI/Ah26JXgdBWaIquA4GxKjaM1P1EDW6mykHnaAVvAR5SgvWuxZpQ2Kcpgmdcf3OzILKM2uElYJ8i/U51z63IKgc/CXhE8tyLwHJhseV7JVr3Al3AcfbtEv0emymRlA4sURoPnALMU3zwVydwsgZiTmB0YisV9rPAEbmhFkQsSj8sN3pMDfP/DW4127LuYKjJCzdT36iMcyQwqsjqjdHqzJLnsvc/BY4qEVSfc31cnSkJV+5ZV4Y9JLNZCn2yl6GRPS513+nKnEQ0y/OBTxgYfgJ6LDD6JIv+ONKjwt7zpgRqRiKF1N5ABzAHWAjcLcm+XALSonS3Yse0pCCR8p1hwDXyZmPrRkCdm0d6QfJ8u6VOO3IDzpMiqNiL1QSSLZuisudCicXEDXdJCa9UJcXHH4sn4+s54BbgCMWMTt2bQR5O9Xg/zdMlKBUsK902z8pz+Fx7jfZ8s4z1gwoH/5m80vTtDRR4sCp7AY6W3PYwQ2wq8vyeVgIFZsubLdDVLcl/tlawpYIx7C6F/Uo1RMzTePTV05MAba56+Vo1H5om+eFhs3Nd5lKh0P3OkOv+0xXzqsskFRt81cPjW8WJm6w8aoebsqlRxdyw8gsTjwebNFFuYh7uHmC1G3yCp4GDUlRF+0DINCmQrU45RisXmyM3XQq9Vu+KC3zuUNUcS0fFRKIP2WyfYeeAVojWcXM8i+Vii4SjecXb5db3L9LvJMkWqx/3VEWiBDlTqfsqnW1XrDlTlwXE83XfofYJkueDyohd50o19Gk3LMrsWFsDb9Wx82y56eYMvtsqsvMV8ZPq/VYdoh6WCYESCthW4C7Z0MfKv5cphsxz1xyt0FWyActJntR7G1O23Zt6r6DAVxeImHkj+38s01w2k2/JFn6x4wapBLMru/9OEsUqJTYZdnxn22/YQAbwD0ShtPhilnUGAAAAAElFTkSuQmCC&style=for-the-badge" alt="">
    <img src="https://img.shields.io/badge/supports%20-openjdk%2022+-gray.svg?colorA=61c265&colorB=4CAF50&style=for-the-badge&logo=java&logoColor=white" alt=""/>
</p>

---

Elixir is an advanced Discord music bot for your server, with support for YouTube, Spotify,
SoundCloud, and raw files. It is powered by LavaPlayer and the Java Discord API (JDA).

Developed with ♥ by Ben Petrillo and KingRainbow44.

---

### Self-Hosting Setup

This section is a work-in-progress and will be completed soon.

### Public REST API

Elixir has its own public API. View the [API documentation](https://docs.benpetrillo.dev).

### All Features

- [x] Support for YouTube and Spotify URLs (playlists included).
- [x] Ability to queue YouTube playlists with up to 600 tracks, Spotify playlists with up to 2,000 tracks. 
- [x] Fully integrated slash commands.
- [x] Loop singular tracks & the entire music queue.
- [x] View the track currently playing, its cover art, artist, duration, etc.
- [x] Fully functional custom playlist system.
    - [x] Custom playlist titles, cover art images, descriptions.
    - [x] Settings (automatically loop, automatic custom volume, title, description).
    - [x] Manually add or remove tracks based on their queue position.
    - [x] Fetch all tracks in any custom playlist.
    - [x] Global system (play anyone else's track).
    - [x] Ability to delete your custom playlists (you cannot delete or modify someone else's).
- [x] Modify player volume.
- [x] Shuffle the guild queue.
- [x] View all tracks in the guild queue.
- [x] Loop a particular track or queue.
- [x] Fetch lyrics of any track.


### License

Elixir is licensed under the [MIT License](https://www.mit.edu/~amini/LICENSE.md).

---

Copyright © 2021-Present Ben Petrillo, KingRainbow44. All rights reserved.