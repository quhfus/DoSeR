Einleitung zu Wikipedia Evidence Mining

1. WikiBlink Engine verwenden um Wikipedie HTML Seite zu generieren.
2. S1HtmlToPlainTextWithEntities verwenden um die HTML Seiten in Plaintext seiten umzuwandeln. Es sind dabei nur noch die Wikipedia interlinks vorhanden.
3. S2PlainTextWithEntitiesToAnnotationList verwenden um Entitäten - AnnotationText Relationen zu erzeugen
4. Verwendung von Hadoop: WikipediaLDADataGeneratorDriver ausführen um alle AnnotationTexte einer Entität zuzuordnen.
5. S3ConstructHBaseEntries verwenden um alle Surface forms to Entitäten und alle Entitäten zu Surface Form zu speichern.
6. S3ConstructHBaseContext verwenden um alle Contexte in HBase zu sammeln, damit besser darauf zugegriffen werden kann.
7. S4CreateCircles verwenden um die Circles zu erstellen. 