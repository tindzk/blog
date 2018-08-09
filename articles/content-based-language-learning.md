---
title     = "A content-based approach to language learning"
published = 2018-08-07
language  = "en-GB"
category  = "nlp"
description = "Ideas for content-based learning with assistive tooling"
---
# Introduction
Language learning is rooted in experience. You start to excel in a language when you have been exposed to a plethora of real-world situations. Therefore, full immersion is often advised, for example by moving to a country where the language is spoken.

In this article, I will describe traditional approaches to language learning and conceptualise a content-based learning environment that leverages assistive tooling. I will outline some of the challenges I encountered in implementing this approach and explore potential solutions.

# Status quo
Current approaches are centred around classroom teaching as well as self-learning courses or programs. Both have their flaws: Classroom teaching offers an unnatural environment with little personalisation. There is a strong focus on grammar rules and specific vocabulary. Furthermore, content in textbooks that teachers use tends to be general and is rarely interesting.

Self-learning courses tend to teach the basics of a language but are hardly enough to satisfy the needs for being able to speak a language fluently. Surely, there are more advanced courses, but these only prove useful in sharpening one's understanding of the language and its subtleties.

Finally, there are numerous computer applications. These mostly target beginners and are thus slow-paced. Recent smartphone apps such as Duolingo aim to incorporate the concept of gamification, but suffer from the same insufficiency as classroom teaching and self-learning courses: lack of authentic content.

# A content-based approach
The initial interest in a language is piqued by the desire to use the language in real-world situations or consume specific literature, music or movies. As we saw in the previous section, this stands in stark contrast to the way how most courses and tools are structured.

To stay motivated one could consume content close to one's interests. Ideally, this would be content that the learners may also readily consume in their mother tongue.

Access to knowledge is the primary reason why people learn English and also succeed in it. Once the content moves to the foreground as opposed to 'learning language X', eventual proficiency merely becomes a side-effect of a continuous effort of using it.

This content-based approach could be classified as more applied than classroom teaching. Although studying vocabulary or grammar is not the focus of this methodology, these may part of the process, but must be derived from the content the learner encountered.

# Content types
We will look at the following four content types that have a high potential to be used in language learning:

* Movies
* Books
* Speech
* Chat

These types complement one another. Thereby, one attains an immersive learning environment that is made up of authentic content.

## Movies
Movies aim to capture the viewer's attention which incentivises the latter to understand the plot and persist in watching until the end. A common practice is to watch movies with English audio and the subtitles set in the target language. English words can be roughly correlated to the words in the subtitles. In the process, the viewer starts to recognise words that repeat themselves.

For having more certainty about the meaning of words in the subtitles, it is still necessary to look them up and verify one's hypotheses. This requires switching the application and typing in the word manually into a dictionary or Google Translate.

Seeing the written words and looking them up alone does not facilitate remembering. It is advisable to copy the word in its context to a text file for later reviewing.

Even though this method is effective and simple, the context switches of using a translation website and then copying over the sentence takes a considerable amount of time and is discouraging, especially for lengthy movies.

## Books
On several occasions, I bought paperback books in a foreign language, most of which I gave up on after only 50-100 pages. The most common reason was the slow process of looking up the words. I would read the book and look up words unbeknownst to me on my smartphone. This process turned out to be rather tedious. As with movies, the context switches were a major distraction, although here it would require switching between completely different media; laying down the book and picking up the smartphone, and vice-versa.

The situation has been slightly improved by e-book readers. These are often equipped with a dictionary app. But these apps malfunction for any language other than English. When tapping a word, the base form would not be inferred.

Another downside is that keeping track of the words encountered is not as convenient as on the laptop and I would skip this step altogether, missing out on the ability to later review the new words.

## Speech: Podcasts and Audiobooks
Self-teaching audio courses often gravitate towards spoken language. I found these scripted dialogues and texts rather limiting. Natural speech is significantly more complex and knowing basic phrases is insufficient for having a regular conversation.

I, therefore, propose to consume solely audio content that is geared towards native speakers. In the process, better foundations are fostered than audio courses could possibly convey. The primary advantage is that this content type can be consumed in various contexts, for example on commutes or travels.

There are two promising candidates for natural speech: Podcasts and audiobooks. Podcasts convey information that is of local interest such as politics, culture, events etc. These are often in the form of discussions and tend to be short, mostly between 20-45 minutes in duration. All these properties make them an excellent fit for language learning. The listener has the capability to immerse oneself in cultural and local topics. Also, the dialogue form of question answering allows one to form language hypotheses when the host asks a question and to test them when the guest answers it.

Audiobooks are significantly harder to understand as they use more literary language and dialogues are less frequent, though a combination of both content types might give the best results.

The problem I had with speech was that, unlike movies, there is no transcript (like subtitles) that can be followed, nor are there any smartphone apps that can show one in real-time.

## Chat
Becoming proficient in a new keyboard takes time and comes with the burden that the layout needs to be changed on the system level which also impacts all other key combinations of programs such as Vi. Also, when chatting with people in different languages, this requires to change not only the system's keyboard layout, but also the browser's spell checker.

Many Slavic languages use the Cyrillic alphabet which poses a major difficulty to learners. I would use Google Translate's transliteration capability to convert Latin letters to their Cyrillic equivalents. Unfortunately, it does not follow strict rules, whereby the transliterations is rather indeterministic.

Alternatively, one could use Latin letters when conversing with native speakers, but this will yield some confusion on behalf of the receiver.

While Android phones have the ability to quickly switch the layout and spell checker, this feature is missing in browsers. Another useful feature of Android is its ability to auto-complete text. This was originally designed to speed up the writing process on touch screens, but it could aid language learners with using the right grammar forms.

# Potential solutions
Most programs can be extended using plug-ins, and open-source programs by modifying their code. Here are some thoughts on how the workflow can be improved to accommodate for the needs of language learners:

For movies, a plug-in for [mpv](https://mpv.io/) could make subtitles clickable. It would look up the phrase or a specific word in Google Translate and remember it together with the start and end time of the utterance. Then, a background daemon could extract the audio segments.

[AntennaPod](http://antennapod.org/) for podcasts and [Voice](https://github.com/PaulWoitaschek/Voice) for audiobooks are the Android apps of my choice. Both are open-source and could be extended with the ability to follow the transcription on-the-fly. The transcription with timings can be obtained by using the [Cloud Speech-to-Text API](https://cloud.google.com/speech-to-text/). This feature would allow to actively follow the spoken utterances and tap unknown words to look these up. Similar to movies, it could remember the timing and extract the context as an audio file.

For Android-based e-book readers, it is possible to modify [FBReader](https://github.com/geometer/FBReaderJ) and equip it with an offline dictionary that can perform stemming. When tapping a word, it could look up the base form or use Google Translate to show context-specific translations.

For chat, a browser extension could integrate with Telegram and other messengers. Depending on the current interlocutor, it would choose the correct keyboard layout and perform transliteration if needed. It might even point out grammar or stylistic errors.

Taking it a step further, a client-server architecture could be devised. Such an architecture should be agnostic to the content types. At the base, it could consist of a centralised server with an API that the different program integrations and plug-ins interact with. This would allow to aggregate all the different data streams in a central place.

The server would store all audio samples, words, phrases and text snippets that the user encounters in different contexts, across different programs and devices.

With this wealth of information, the server can generate personalised flashcards. This is presumably more effective than taking off-the-shelf decks as the learner has an active memory of the context in which the sentences appeared. Another advantage of combining different data sources is that the system could build a profile of the user and track the progress.

# Conclusion
I described how improvements in methodology and tooling could make language learning more effective.

At the moment, there is a general lack of integration between tools. As a consequence, there are context switches and repetitive, manual labour. The lack of assistive features such as auto-completions, ability to look up words, remember audio segments or the textual context slow down learning with real content immensely.

So far I only outlined one component of language learning, albeit the most important as it lays the foundations for one's understanding and ability to read and write speak. Nevertheless, a content-based approach is incomplete without the study of rules and conversational practice.

I have experimented with some ideas and plan to develop these further. There are more content types to be explored and I hope to turn some of these into projects.
