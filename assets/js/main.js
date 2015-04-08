﻿/**
JW Clark

load each XML to DOMParser
---
onLoadFiles
reset
parseXML
parseSentences
parseTokens
tfidf
print

started at  http://www.html5rocks.com/en/tutorials/file/dndfiles/
learned a closure - http://stackoverflow.com/questions/12546775/get-filename-after-filereader-asynchronously-loaded-a-file
*/


var totalDocumentsProcessed = 0;
var totalDocumentCount = 0;			//set by `onFilesSelected`
var totalTokenCount = 0;			//incremented by `incrementTokenCounts`
var totalSentenceCount = 0;
var totalSentimentScore = 0;
var mapDocuments = {};
var mapTokens = {};
var discardTokens = [];

function reset() {
	totalDocumentsProcessed = 0;
	totalDocumentCount = 0;
	totalTokenCount = 0;
	totalSentenceCount = 0;
	totalSentimentScore = 0;
	mapDocuments = {};
	mapTokens = {};
	discardTokens = [];
	$('output').html('');
	$('ol').html('');
}

function SentimentFormatException(sentiment) {
	this.value = sentiment;
	this.message = "Unhandled sentiment expression";
	this.toString = function () {
		return this.value + " : " + this.message;
	};
}

function evaluateSentimentLabelScore(sentimentLabel) {
	switch (sentimentLabel) {
		case "Verypositive":
			return 2;
		case "Positive":
			return 1;
		case "Neutral":
			return 0;
		case "Negative":
			return -1;
		case "Verynegative":
			return -2;
		default:
			throw new SentimentFormatException(sentimentLabel);
			break;
	}
}

function print() {
	//sort token list by tfidf
	var sortable = [];
	for (var key in mapTokens) {
		sortable.push([key, mapTokens[key].tfidf]);
	}
	sortable.sort(function (a, b) { return b[1] - a[1] }); //max to min

	//print sorted token tfidf list
	var tokenList = $('#token-list');
	for (var i = 0; i < sortable.length; i++) {
		$(tokenList).append('<li>' + sortable[i][0] + " : " + (sortable[i][1]).toFixed(6) + '</li>');
	}

	//print documents and sentences one token at at time
	var html = '';
	var container = $('#container-documents');
	for (var key in mapDocuments) {
		var sentences = mapDocuments[key].sentences;
		for (var i = 0; i < sentences.length; i++) {
			var index = sentences[i].index;
			var label = sentences[i].sentimentLabel;
			var tokens = sentences[i].tokens;
			html += '<div>' + index + ': ' + label + '</div><div>';
			for (var k = 0; k < tokens.length; k++) {
				html += '<span ';
				var word = tokens[k].word;
				var tfidf = '';
				if (word in mapTokens) {
					tfidf = mapTokens[word].tfidf;
					var colorScale = Math.floor(100 * tfidf * 255);
					var yellow = rgb(colorScale, colorScale, 0); //scale to a shadow of yellow out of max 255 inverted
					var red = rgb(colorScale, 0, 0);
					html += 'style = "border-radius: 3px; padding: 1px 3px 1px 2px; color: white; background-color: ' + red + '"';
				}
				var pos = tokens[k].pos;
				html += 'title="' + pos + ': ' + tfidf + '" data-tfidf="' + tfidf + '" data-pos="' + pos + '">' + word + '</span>&nbsp;';
			}
			html += '</div><br>';
		}
		html += '<hr>';
	}
	$(container).html(html);
}

function tfidf() {
	for (var key in mapTokens) {
		var thisTokenCount = mapTokens[key].value;
		var numFilesWithToken = mapTokens[key].set.size;
		var tf = thisTokenCount / totalTokenCount;
		var idf = Math.log10(totalDocumentCount / numFilesWithToken);
		mapTokens[key].tf = tf;
		mapTokens[key].idf = idf;
		mapTokens[key].tfidf = tf * idf;
	}
	console.log("tfidf: " + mapTokens);
}

function incrementTokenCounts(token) {
	var target = token.word;
	//alphanumeric, length > 1, not I
	if ((target.match(/^[0-9a-z]+$/) && target.length > 1) || target === "i".toUpperCase()) {
		if (!(target in mapTokens)) {
			//create a new token
			mapTokens[target] = {};
			mapTokens[target].value = 1;
			mapTokens[target].set = new Set();
			mapTokens[target].set.add(token.documentId);
		} else {
			//increment and existing token
			mapTokens[target].value++;
			mapTokens[target].set.add(token.documentId);
		}
		totalTokenCount++;
	} else {
		discardTokens.push(target);
	}
}

function parseTokens(sentence) {
	var data = []; //temp array
	var iWord = 0, iLemma = 1, iPos = 4, iNer = 5, iSpeaker = 6; //indexes
	var tokens = sentence.xml.children[0].children;
	for (var i = 0; i < tokens.length; i++) {
		var token = {};
		token.index = i;
		token.sentenceIndex = sentence.index;
		token.documentId = sentence.documentId;
		token.word = tokens[i].children[iWord].textContent;
		token.lemma = tokens[i].children[iLemma].textContent;
		token.pos = tokens[i].children[iPos].textContent;
		//ignore stop words
		if (!isStopWord(token.word))
			incrementTokenCounts(token);
		data.push(token);
	}
	return data;
}

function parseSentences(document) {
	var data = []; //temp array
	var sentenceXML = document.xml.getElementsByTagName('sentences')[0].children;
	for (var i = 0; i < sentenceXML.length; i++) {
		var sentence = {};
		sentence.index = i;
		sentence.documentId = document.id;
		sentence.xml = sentenceXML[i];
		sentence.sentimentScore = parseInt(sentenceXML[i].attributes[1].nodeValue);
		sentence.sentimentLabel = sentenceXML[i].attributes[2].nodeValue;
		sentence.tokens = parseTokens(sentence);
		data.push(sentence);
	}
	return data;
}

function parseXML(evt, file) {
	var key = file.name;
	var value = evt.target.result;
	var parser = new DOMParser();
	var xml = parser.parseFromString(value, "application/xml")
	var document = {};
	document.id = key;
	document.xml = xml;
	document.sentences = parseSentences(document);

	mapDocuments[key] = document;

	if (++totalDocumentsProcessed === totalDocumentCount) {
		tfidf();
		print();
		/* should probably treat sentences as documents when there is only one file available
		 * else all tfidf values will be 0: log10(1/1)
		 * if (totalDocumentCount === 1) { } */
	}
	console.log(mapDocuments[key]);
}

function onFilesSelected(event) {
	reset();
	var files = event.target.files; // FileList object
	totalDocumentCount = files.length;
	for (var i = 0, f; f = files[i]; i++) {
		var fileReader = new FileReader();
		fileReader.onloadend = (function (file) {
			return function (evt) {
				parseXML(evt, file)
			}
		})(f);
		fileReader.readAsText(f);
	}
	console.log("discard: " + discardTokens);
}

document.getElementById('files').addEventListener('change', onFilesSelected, false);

function rgb(r, g, b) {
	return "rgb(" + r + "," + g + "," + b + ")";
}

function isStopWord(token) {
	//http://www.ranks.nl/stopwords
	//maybe transpose this in excel then paste it back here
	//http://www.extendoffice.com/documents/excel/681-excel-change-columns-to-rows.html
	var stopWords = [
		"a",
		"about",
		"above",
		"after",
		"again",
		"against",
		"all",
		"am",
		"an",
		"and",
		"any",
		"are",
		"aren't",
		"as",
		"at",
		"be",
		"because",
		"been",
		"before",
		"being",
		"below",
		"between",
		"both",
		"but",
		"by",
		"can't",
		"cannot",
		"could",
		"couldn't",
		"did",
		"didn't",
		"do",
		"does",
		"doesn't",
		"doing",
		"don't",
		"down",
		"during",
		"each",
		"few",
		"for",
		"from",
		"further",
		"had",
		"hadn't",
		"has",
		"hasn't",
		"have",
		"haven't",
		"having",
		"he",
		"he'd",
		"he'll",
		"he's",
		"her",
		"here",
		"here's",
		"hers",
		"herself",
		"him",
		"himself",
		"his",
		"how",
		"how's",
		"i",
		"i'd",
		"i'll",
		"i'm",
		"i've",
		"if",
		"in",
		"into",
		"is",
		"isn't",
		"it",
		"it's",
		"its",
		"itself",
		"let's",
		"me",
		"more",
		"most",
		"mustn't",
		"my",
		"myself",
		"no",
		"nor",
		"not",
		"of",
		"off",
		"on",
		"once",
		"only",
		"or",
		"other",
		"ought",
		"our",
		"ours",
		"ourselves",
		"out",
		"over",
		"own",
		"same",
		"shan't",
		"she",
		"she'd",
		"she'll",
		"she's",
		"should",
		"shouldn't",
		"so",
		"some",
		"such",
		"than",
		"that",
		"that's",
		"the",
		"their",
		"theirs",
		"them",
		"themselves",
		"then",
		"there",
		"there's",
		"these",
		"they",
		"they'd",
		"they'll",
		"they're",
		"they've",
		"this",
		"those",
		"through",
		"to",
		"too",
		"under",
		"until",
		"up",
		"very",
		"was",
		"wasn't",
		"we",
		"we'd",
		"we'll",
		"we're",
		"we've",
		"were",
		"weren't",
		"what",
		"what's",
		"when",
		"when's",
		"where",
		"where's",
		"which",
		"while",
		"who",
		"who's",
		"whom",
		"why",
		"why's",
		"with",
		"won't",
		"would",
		"wouldn't",
		"you",
		"you'd",
		"you'll",
		"you're",
		"you've",
		"your",
		"yours",
		"yourself",
		"yourselves"
	];
	if(stopWords.indexOf(token) > -1)
		return true;	
	else
		return false;
}