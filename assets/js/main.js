/**

JW Clark

load XML from file upload, parse it

started at  http://www.html5rocks.com/en/tutorials/file/dndfiles/
learned a closure - http://stackoverflow.com/questions/12546775/get-filename-after-filereader-asynchronously-loaded-a-file

*/


var totalDocumentsProcessed = 0;
var totalDocumentCount = 0;
var totalWordCount = 0;
var totalLemmaCount = 0;
var totalSentenceCount = 0;
var totalSentimentScore = 0;
var collectDocuments = {};
var collectLemmas = {};
var collectWords = {};
var discardWords = [];
var discardLemmas = [];

function reset() {
	totalDocumentsProcessed = 0;
	totalDocumentCount = 0;
	totalWordCount = 0;
	totalLemmaCount = 0;
	totalSentenceCount = 0;
	totalSentimentScore = 0;
	collectDocuments = {};
	collectLemmas = {};
	collectWords = {};
	discardWords = [];
	discardLemmas = [];
	$('output').html('');
	$('ol').html('');
}

function print() {
	var sortable = [];
	for (var key in collectWords)
		sortable.push([key, collectWords[key].tfidf]);
	sortable.sort(function (a, b) { return b[1] - a[1] });
	console.log(sortable);
	var lemmaList = $('#lemma-frequency');
	for (var i = 0; i < sortable.length; i++) {
		$(lemmaList).append('<li>' + sortable[i][0] + " : " + (sortable[i][1]).toFixed(6) + '</li>');
	}
}

function tfidf() {
	//var lemmaList = $('#lemma-frequency'); 
	for (var key in collectLemmas) {
		if (collectLemmas.hasOwnProperty(key)) {
			var precision = 5;
			var lemmaCount = collectLemmas[key].value;
			var numFilesContainingLemma = collectLemmas[key].set.size;
			var tf = lemmaCount / totalLemmaCount;
			var idf = Math.log10(totalDocumentCount / numFilesContainingLemma);
			var tfidf = tf * idf;
			collectLemmas[key].tfidf = tfidf;
			//$(lemmaList).append('<li>' + key + ': ' + tf + ' : ' + idf + '</li>');
		}
	}


	//var wordList = $('#word-frequency');
	for (var key in collectWords) {
		if (collectWords.hasOwnProperty(key)) {
			var wordCount = collectWords[key].value;
			var numFilesContainingWord = collectWords[key].set.size;
			var tf = wordCount / totalWordCount;
			var idf = Math.log10(totalDocumentCount / numFilesContainingWord);
			var tfidf = tf * idf;
			collectWords[key].tfidf = tfidf;
			//$(wordList).append('<li>' + key + ': ' + tf + ' : ' + idf + '</li>');
		}
	}
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

function incrementLemmaCounts(lemma, fileKey) {
	if (lemma.match(/^[0-9a-z]+$/) || lemma.length > 1 || lemma === "i".toUpperCase()) {
		if (!(lemma in collectLemmas)) {
			collectLemmas[lemma] = {};
			collectLemmas[lemma].value = 1;
			collectLemmas[lemma].set = new Set();
			collectLemmas[lemma].set.add(fileKey);
		} else {
			collectLemmas[lemma].value++;
			collectLemmas[lemma].set.add(fileKey);
		}
		totalLemmaCount++;
	}
	else
		discardLemmas.push(lemma);
}

function incrementWordCounts(word, fileKey) {
	if (word.match(/^[0-9a-z]+$/) || word.length > 1 || word === "i".toUpperCase()) {
		if (!(word in collectWords)) {
			collectWords[word] = {};
			collectWords[word].value = 1;
			collectWords[word].set = new Set();
			collectWords[word].set.add(fileKey);
		} else {
			collectWords[word].value++;
			collectWords[word].set.add(fileKey);
		}
		totalWordCount++;
	}
	else
		discardWords.push(word);
}


function parseCoreNLPXML() {

	//foreach document
	for (var fileName in collectDocuments) {
		if (collectDocuments.hasOwnProperty(fileName)) {

			var parser = new DOMParser();
			var xml = parser.parseFromString(collectDocuments[fileName], "application/xml");
			var sentences = xml.getElementsByTagName('sentences')[0].children;
			var html = '';

			//foreach sentence
			for (var i = 0; i < sentences.length; i++) {
				var sentimentScore = parseInt(sentences[i].attributes[1].nodeValue);
				var sentimentLabel = sentences[i].attributes[2].nodeValue;
				var tokens = sentences[i].children[0].children;

				//foreach token
				var tokenSentence = '';
				for (var k = 0; k < tokens.length; k++) {
					var iWord = 0;
					var iLemma = 1;
					var iPos = 4;
					var iNer = 5;
					var iSpeaker = 6;
					var word = tokens[k].children[iWord].textContent;
					var lemma = tokens[k].children[iLemma].textContent;
					incrementWordCounts(word, fileName);
					incrementLemmaCounts(lemma, fileName);
					tokenSentence += word + ' ';
				}
				totalSentimentScore += evaluateSentimentLabelScore(sentimentLabel);
				totalSentenceCount++;

				html += '<br><div>' + (i + 1) + ': ' + sentimentLabel + '</div><div>' + tokenSentence + '</div>';
			}
			var containerDocument = '<div id="' + fileName + '">' + html + '</div><br><hr>';
			$('#totalSentimentScore').html('<b>Total Sentiment Score:</b> ' + totalSentimentScore);
			$('#totalWordCount').html('<b>Word Count:</b> ' + totalWordCount);
			$('#totalLemmaCount').html('<b>Lemma Count:</b> ' + totalLemmaCount);
			$('#totalSentences').html('<b>Sentences:</b> ' + totalSentenceCount);
			$('#container-documents').append(containerDocument);
		}
	}

	tfidf();
	print();

	$('#display').show();
	console.log(discardWords);
	console.log(discardLemmas);
	console.log(collectWords);
}


function handleFileSelect(event) {
	reset();
	var files = event.target.files; // FileList object
	totalDocumentCount = files.length;
	for (var i = 0, f; f = files[i]; i++) {
		var fileReader = new FileReader();
		fileReader.onloadend = (function (file) {
			return function (evt) {
				collectDocument(evt, file)
			}
		})(f);
		fileReader.readAsText(f);
	}
}

function collectDocument(evt, file) {
	collectDocuments[file.name] = evt.target.result;
	if (++totalDocumentsProcessed === totalDocumentCount)
		parseCoreNLPXML();
}

document.getElementById('files').addEventListener('change', handleFileSelect, false);