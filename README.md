# liferayAjaxAutoCompleteJournalArticleSearch

There is a searchbox that does an ajax call to the Liferay Search (based on ElasticSearch) API, that returns a JSON with 
the URL to the document and the document's title.

TODO:

It is not optimal to search for the actual journal article for all the documents (on the database).
It would be more optimal to be lazy and postpone the url search to when the person does the click
