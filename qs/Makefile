

ASSETS = anhang/assets
PARTIALS = anhang/partials

all: dirs qs-dokument.pdf

clean:
	rm -rf $(ASSETS) $(PARTIALS)

dirs:
	mkdir -p $(ASSETS) $(PARTIALS)


$(PARTIALS)/codereviews.tex: belege/code-reviews/*.md
	cat $^ | pandoc -r markdown -w latex | sed 's/\\subsection/\\clearpage\\subsection/' > $@

$(PARTIALS)/manuelle-tests-ui-reports.tex: belege/manuelle-tests/ui/*.md
	cat $^ | pandoc -r markdown -w latex | sed 's/\\subsection/\\clearpage\\subsection/' > $@

$(PARTIALS)/dokumentation.tex: ../docs/Dokumentation.md
	pandoc -r markdown -w latex -o $@ $<

$(PARTIALS)/coverage.pdf: belege/coverage/index.html
	wkhtmltopdf $< $@

$(PARTIALS)/junit.pdf: belege/junit/bonfire-junit-report.html
	wkhtmltopdf $< $@

$(PARTIALS)/lint-results%.pdf: belege/lint/lint-results%.tmp.html
	wkhtmltopdf --lowquality $< $@

belege/lint/lint-results%.tmp.html: belege/lint/lint-results%.html
	cat $< > $@
	echo "<style> body,pre {background: white; } body,*{ color: black} body a {color:#00a!important} </style>" >> $@

qs-dokument.pdf: qs-dokument.tex anhang/*.tex spezifikation/*.tex \
		$(PARTIALS)/codereviews.tex $(PARTIALS)/manuelle-tests-ui-reports.tex $(PARTIALS)/dokumentation.tex \
		$(PARTIALS)/coverage.pdf $(PARTIALS)/junit.pdf $(PARTIALS)/lint-results-1.pdf $(PARTIALS)/lint-results-2.pdf
	pdflatex qs-dokument.tex
	pdflatex qs-dokument.tex

open:
	xdg-open qs-dokument.pdf

rebuild: clean all

.PHONY: dirs clean
