---
title       = "Transliteration techniques"
published   = 2018-09-04
language    = "en-GB"
category    = "nlp"
description = "Use cases and techniques for transliteration"
---

# Introduction
The word *transliteration* is derived from the morphemes *trans-* (across, through) and *liter-* (letter). It denotes the translation from one alphabetic writing system into another. As an example, the city of Харків in Ukraine written in the Cyrillic script is commonly transliterated as either *Kharkiv* or *Harkiv* in the Latin script. This single example shows that there is no single mapping. The standard alphabet of any language is always optimised to accommodate its phonotactic peculiarities. Note that this does not necessarily imply that the alphabet is also the most compact. For example, a more optimised alphabet for English would use single letters for *ts*, *sh* and *ch*.

In this article, we will look at use cases for transliteration and explore various considerations to be made when designing rules. To this end, I will present the [translit-scala](https://github.com/sparsetech/translit-scala) library. Its development informed many of the examples and best practices outlined in this article.

# Use cases
The most common use of transliteration is for foreign entities. In Russian, the name *Steve Jobs* would be written as *Стив Джобс*. It does not adhere to the English spelling, but rather to the sounds. This phenomenon also applies to cities, products and other non-Russian names. The motivation is that it allows for a more natural integration of words and their adaptation to Russian grammar, e.g. declension. For example, *Steve Jobs' book* is translated as *книга Стива Джобса*. The word suffix `-а` is a case marker which has the same possessive role as the **'s** or **s'** in English. In Latin-script Slavic languages the original spelling of foreign words is retained and the case marker is added. In Polish, the convention is to use an apostrophe to modify words with a trailing vowel letter: *Steve'a Jobsa* or *Harry'ego Pottera*.

International passports are the most prominent use of transliteration in the other direction. These contain a person's name in the original (e.g. Cyrillic) and Latin script. To avoid ambiguities, governments issue rules on how to correctly transliterate names.

Another use case are domain names. Although Unicode domains are widely supported, Latin-script domains are significantly more widespread. The same goes for URLs. Unicode characters can be used in URLs too, but in both cases, typing in a Unicode domain or URL would require changing the keyboard layout. An even more fundamental assumption is that the user is proficient in the alphabet, which is not always the case.

Search Engine Optimisation (SEO) is about improving the visibility of a website in search engines. Users who search for content using transliterated letters will receive more results if website content is transliterated. Furthermore, when copying links, browsers encode them as follows: `https://ru.wikipedia.org/wiki/%D0%A1%D0%BF%D0%B8%D1%81%D0%BE%D0%BA_%D0%BB%D0%B0%D1%82%D0%B8%D0%BD%D1%81%D0%BA%D0%B8%D1%85_%D0%B1%D1%83%D0%BA%D0%B2` With transliteration, the URL would still be readable and considerably shorter, e.g. `https://ru.wikipedia.org/wiki/Spisok_latinskih_bukv`. Server-side software provides the ability to set up an alias, so both links would be valid.

In Natural Language Processing (NLP), there are two use cases: Machine translation and disambiguation. Foreign names are often transliterated, as seen before. Disambiguation is the reverse operation: for example, in a travel or e-commerce chatbot, users may write a product or city name in either the original or transliterated spelling. Rather than accepting only exact matches, the chatbot could generate candidate transliterations.

Two characters looking exactly the same may still denote different letters. For example, p denotes "Latin Small Letter P" and has the code U+0070, whereas р is "Cyrillic Small Letter Er" and its code is U+0440. Confusing two similar-looking letters from different scripts is likely to happen when dealing with multiple keyboards and languages. This underlines the importance of disambiguation of user input, such as in the chatbot example.

Finally, transliteration could prove useful in assistive technology. When starting to learn a language, seeing transliterated words will help users acquire the new alphabet faster. Having a special transliteration keyboard could be advantageous to language learners, as mastering a new keyboard takes time. This also avoids mental switching, and could be even useful for non-English native speakers who are mostly working in an English context.

All these use cases have in common that they increase accessibility in one way or another, which is why transliteration can be seen as a valuable augmentation of a language's original script.

# Classification
Transliteration can be reduced to a translation problem. It concerns itself with the question of how to map letters from one alphabet into another. Transliteration to Latin is also called *romanisation*, derived from *Romance* languages, e.g. Italian, French, Spanish, etc.

Transliteration is a challenging problem as there is not always a direct equivalent for letters from two distinct alphabets. As an example, English has 26 letters, whereas Russian uses the Cyrillic alphabet and has 33. Other languages like Ukrainian, Bulgarian and Serbian also use Cyrillic letters, but have subtle differences which even extend to the pronunciation of particular letters. For instance, the letter и is pronounced differently in Russian and Ukrainian.

We can conclude that unique rules must be specified for individual languages and they need to account not only for spelling, but also pronunciation differences.

# Rule-based approach
Languages follow inherent patterns, such as certain letter and sound combinations being more common than others. The most accurate way to transcribe words in a language is to use its official alphabet, but by modelling syntactic and phonetic patterns we can project words even onto other language alphabets. Suffice it to say, there is no single ground of truth in transliteration. Models can be of arbitrary complexity, ranging from hand-engineered character-level replacement rules, to Machine Learning models.

Character-level replacement rules may look as follows:

* X → Х
* а → a
* p → р
* k → к
* etc.

This generally works well, but there are too many exceptions when considering single characters. Throughout this article, we will use a simple yet effective model that performs replacements of n-grams. For the languages at hand, bi- and trigrams cover a large proportion of vocabulary. These n-grams serve the purpose of approximating morphemes. As with other NLP tasks, these simple models tend to achieve accuracies competitive with more sophisticated Machine Learning models.

The upside of such a simple model based on n-gram pattern replacements is that it is straightforward to implement and reason about. With the advent of Machine Learning, researchers have also explored approaches based on Recurrent Neural Networks, which can even deal with noise in the input, such as spelling mistakes. These certainly have upsides, but equally require more computational resources and behave non-deterministically in some situations.

## Rule types
Rules can be derived by establishing a relationship between the source and target alphabet. A rule can be based on either:

1. [Grapheme](https://en.wikipedia.org/wiki/Grapheme) similarity, or
2. [Phoneme](https://en.wikipedia.org/wiki/Phoneme) similarity

An example for grapheme similarity is the Latin letter *m*, which could be mapped onto the Cyrillic *м*, or the letter *b*, which could be mapped onto *в*.

Phoneme similarity, on the other hand, establishes a relationship based on sounds: for example, the Cyrillic *л* is mapped onto *l*. It is said that these two letters are homophones. There are also Cyrillic letters without a straightforward English analogue: *x* is commonly mapped onto *h* (as in *house*) or *kh* (pronunced like *ch* in *loch*).

For instance, the Russian *hacker* (хакер) can be transliterated as either *xakep* (graphemes) or *haker* (phonemes).

The most intuitive approach is to default to phoneme similarity and provide additional convenience mappings based on grapheme similarity, for letters that would be unmapped otherwise.

## Function classes and directionality
In mathematics, the concept of function classes is established. A function that describes a mapping from one set to another can be either injective, surjective or bijective.

Bijective and surjective mappings make most sense in the context of transliteration, since every letter from the target alphabet (co-domain) should be mapped to by at least one letter from the source alphabet (domain).

Another concern is the directionality. If `f(s)` maps a word from one alphabet `S` onto another `T` and `g(t)` denotes the opposite direction with `t ∈ T`, then the transliteration is said to be reversible if `∀s ∈ S: s = g(f(s))`. This is only possible for bijective functions.

However, there are good reasons for surjective mappings, i.e. having multiple transliterations for a single letter. We can relax the constraint by normalising the input: `∀s ∈ S: norm(s) = g(f(s))`. Thus, `norm(s)` would define a primary transliteration for each character in `s` and alternative (alias) ones. `g(t)`, respectively, would have to return the primary transliteration for each character.

Note that the set of characters must comprise not only letters but also special diacritics such as accents and soft/hard signs. This poses some additional difficulties, but is worth accounting for.

The question of directionality is important, since government-issued rules often tend to be lossy and are only unidirectionally defined. This is especially true when choosing a phonetic approach, e.g. the [Ukrainian rule set](http://zakon1.rada.gov.ua/laws/show/55-2010-%D0%BF) transliterates the name *Соломія* as *Solomiia*, with *і* mapped onto *і*, and *я* onto *ia*. However, *Юрій* is transliterated as *Yurii*. As can be seen from these examples, the Latin *i* has three possible transliterations depending on the context. These rules are difficult to reverse. With a few minor changes, e.g. encoding *я* using a less-overloaded letter (*ya*) and for *й* using a separate letter *j*, the complexity of the model can be reduced. Then, the names would be transliterated as *Yurij* and *Solomiya*, which are still sufficiently readable.

## Conditional probabilities
The morphology of each language obeys inherent patterns. Using a corpus, we can derive probabilities for how likely the next letter is given the context; formally, `P(s | c)` where `s` is any letter from the alphabet and `c` the left context prior to `s`.

If the target alphabet has more letters than the source alphabet, it may be desirable to re-use certain characters in different contexts and have the transliteration mechanism choose the correct one. As an alternative, one may use bi- or trigrams. A conditional model can still be useful to test hypotheses about the likelihood of certain n-grams occurring. In the event of the n-gram replacement being ambiguous, the model could correct mistakes without explicit feedback from the user, for example in the form of a precedence delimiter.

## Considerations and trade-offs
When coming up with a set of rules, several factors are to be considered:

* **Consistency:** There likely are already some established transliteration rules for a language. These may be taken as a basis.
* **Frequency:** The more common a letter is, the shorter its mapped n-gram should be.
* **Special characters:** Every character should be mapped, including soft/hard signs, apostrophes, etc. Otherwise, the transliteration is not reversible.
* **Conflicts:** Two Latin letters may correspond to several letters in the target language (homophones).
* **Keyboard position:** A common letter should be easy to type. The key travel distance of common sequences should be considered.
* **Shortcuts:** If there any unused Latin letters, these could be bound to long n-grams for convenience. For example, *q* could be mapped onto *щ* (shch) and *w* onto *ш* (sh), as these two letters would have no meaning otherwise in Cyrillic languages.
* **Ambiguous mappings:** Can certain letters have several meanings in different contexts? Use an adaptive approach that looks at the context, or introduce a precedence separator for disambiguation.
* **Lookahead:** Does the rule require a lookahead, i.e. the right context? This should be avoided if possible as it makes on-the-fly transliteration harder.

It turns out that we are looking at an optimisation problem that has many constraints. In my work so far, this optimisation step was performed manually by trial-and-error and using the transliteration schema in real-world scenarios. It would be valuable to explore automated approaches to find alternative, more optimal rules.

# Implementation
The transliteration mechanism was implemented as a Scala library, which can be found [here](https://github.com/sparsetech/translit-scala). Currently, it has support for Russian and Ukrainian.

To limit the scope of this article, let us briefly explore how the Ukrainian alphabet is mapped. It is phonetic at its base, but with several extensions.

Unigrams:

| a | b | d | e | f | g | h | i | j | k | l | m | n | o | p | r |
|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| а | б | д | е | ф | г | х | і | й | к | л | м | н | о | п | р |

| s | t | u | v | y | z |
|---|---|---|---|---|---|
| с | т | у | в | и | з |

There are convenience mappings which are optimised for the English (US) keyboard layout:

| c | q | w | x |
|---|---|---|---|
| ц | щ | ш | ж |


Bigrams:

| ya | ye | yi | yu | g' | ch | sh | ts | zh |
|----|----|----|----|----|----|----|----|----|
| я  | є  | ї  | ю  | ґ  | ч  |  ш | ц  | ж  |

There is a precedence separator, `|`, which is used to prevent these bigrams from being applied. This is needed for some words such as *схильність* (*s|hyl'nist'*).

There are no trigrams, but there is one 4-gram:

| shch |
|------|
| щ    |

Unlike Russian, Ukrainian does not have a hard sign. Instead, it has a soft sign and an apostrophe. There is experimental support for disambiguating apostrophes by looking at the context. This only works if the left and right context is available. For example, the word *п'ять* (English: *five*), corresponds to *p'yat'*. The first apostrophe comes from an apostrophe, whereas the second comes from a soft sign.

The above rules converged after numerous modifications from the [official *National 2010* government rules](http://zakon1.rada.gov.ua/laws/show/55-2010-%D0%BF). For more information on the reasoning behind the mappings, please refer to the project page.

## Library
The library can be used as follows:

```scala
translit.Ukrainian.latinToCyrillic("idu do domu")
  // іду до дому
```

Under the hood, it uses the function `latinToCyrillicOne` to transliterate the current character given its left and right context. For the second character from the input above, the call looks as follows:

```scala
translit.Ukrainian.latinToCyrillicOne("i", 'd', "u do domu")
  // (0, 'д')
```

The first element of the tuple indicates how many characters relative to the offset need to be replaced by the mapped character. If it is a negative value, it indicates the number of characters in the left context.

For the input *shcho*/*що* (translation: 'what'), it would be desirable to receive on-the-fly replacements while the user is typing. Hence, the function would return the following values for each character:

```
( 0, 'с')
(-1, 'ш')
( 0, 'ц')
(-2, 'щ')
( 0, 'о')
```

If these incremental changes are correctly applied, the output will be *що*.

So far, only the direction Latin to Cyrillic has been implemented.

The library is cross-compiled to JavaScript, JVM and LLVM bytecode, which allows the same logic to be used in a wide variety of languages.

# Future work

## Testing
There are a number of unit tests that already ensure that the transliteration rules behave as expected. However, a corpus-linguistic approach would give further confidence.

After the reverse direction has been implemented, i.e. Cyrillic to Latin, we could use property-based testing and check whether the rules are well-behaved for a large corpus such as a Wikipedia text dump.


## Optimisation
There are [endeavours](http://mkweb.bcgsc.ca/carpalx/?typing_effort) to optimise keyboard layouts, for example by modelling typing effort. While transliteration is agnostic to the keyboard layout, we can assume that it will be used with the QWERTY layout. The mappings could be optimised by taking into account texts from a large corpus, and measuring different metrics such as number of letters needed or finger travel distance for the letters in a word.

## Machine Learning
So far the mapping rules have been manually defined. Another approach would be to use a sequence-to-sequence model such as LSTM. There are two use cases:

1. **Error correction:** This would allow users to make mistakes and diverge from the rules.
2. **Disambiguation:** We could define more compact rules that use more unigrams rather than `(n > 1)`-grams. Handling all the exceptions and conflicts by hard-coding them is cumbersome.

As demonstrated by Google Translate, these models can be error-prone and unpredictable, so they should be opt-in.

Some related work for using LSTM with Armenian can be found [here](https://yerevann.github.io/2016/09/09/automatic-transliteration-with-lstm/).

## Contributing
The priority is to capture a large portion of use cases to make translit-scala production-ready. To help with this, feel free to point out any inconsistencies or examples where the current rules conflict.

So far, translit-scala only has a single set of rules for each language, but as end users have different mother tongues and use cases, it makes sense to support multiple layouts. Please feel free to contribute transliteration models for already-supported or other languages.
