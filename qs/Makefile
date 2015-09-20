

ASSETS = anhang/assets
PARTIALS = anhang/partials

all: dirs qs-dokument.pdf

clean:
	rm -rf $(ASSETS) $(PARTIALS)

dirs:
	mkdir -p $(ASSETS) $(PARTIALS)

$(PARTIALS)/codereviews.tex: belege/code-reviews/*.md
	cat $^ | pandoc -r markdown -w latex -o $@
	sed -i '' 's/\\subsection/\\clearpage\\subsection/' $@

$(PARTIALS)/manuelle-tests-ui-reports.tex: belege/manuelle-tests/ui/*.md
	cat $^ | pandoc -r markdown -w latex -o $@
	sed -i '' 's/\\subsection/\\clearpage\\subsection/' $@

$(ASSETS)/05-documentation.md: bp.wiki/Dokumentation.markdown
	cp $< $@

$(PARTIALS)/coverage.pdf: belege/coverage/index.html
	wkhtmltopdf $< $@

$(PARTIALS)/junit.pdf: belege/junit/bonfire-junit-report.html
	wkhtmltopdf $< $@

$(PARTIALS)/lint-results.pdf: belege/lint/lint-results-prep.html
	wkhtmltopdf --lowquality $< $@

belege/lint/lint-results-prep.html: belege/lint/lint-results.html
	cat $< > $@
	echo "<style> body,pre {background: white; } body,*{ color: black} body a {color:#00a!important} </style>" >> $@

qs-dokument.pdf: qs-dokument.tex anhang/anhang.tex spezifikation/*.tex $(PARTIALS)/codereviews.tex $(PARTIALS)/manuelle-tests-ui-reports.tex \
									$(PARTIALS)/coverage.pdf $(PARTIALS)/junit.pdf $(PARTIALS)/lint-results.pdf
	pdflatex qs-dokument.tex
	pdflatex qs-dokument.tex

open:
	xdg-open qs-dokument.pdf

rebuild: clean all

.PHONY: dirs clean
