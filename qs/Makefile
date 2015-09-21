

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

$(PARTIALS)/javadoc.pdf: belege/junit/javadoc.html
	cd toolchain && ./make-javadoc-html.sh
	wkhtmltopdf $< $@

$(PARTIALS)/junit.pdf: belege/junit/bonfire-junit-report.html
	wkhtmltopdf $< $@

$(PARTIALS)/checkstyle%.pdf: belege/checkstyle/checkstyle%.xml.html
	cd toolchain && ./make-checkstyle-html.sh
	wkhtmltopdf --lowquality $< $@

$(PARTIALS)/lint-results%.pdf: belege/lint/lint-results%.tmp.html
	wkhtmltopdf --lowquality $< $@

belege/lint/lint-results%.tmp.html: belege/lint/lint-results%.html
	cat $< > $@
	echo "<style> body,pre {background: white; } body,*{ color: black} body a {color:#00a!important} </style>" >> $@

$(PARTIALS)/findbugs%.pdf: belege/findbugs/findbugs%.html
	wkhtmltopdf --lowquality $< $@

qs-dokument.pdf: qs-dokument.tex anhang/*.tex spezifikation/*.tex \
		$(PARTIALS)/codereviews.tex $(PARTIALS)/manuelle-tests-ui-reports.tex $(PARTIALS)/dokumentation.tex \
		$(PARTIALS)/coverage.pdf $(PARTIALS)/javadoc.pdf $(PARTIALS)/junit.pdf $(PARTIALS)/checkstyle-1.pdf \
		$(PARTIALS)/lint-results-1.pdf $(PARTIALS)/lint-results-2.pdf $(PARTIALS)/findbugs-1.pdf
	pdflatex qs-dokument.tex
	pdflatex qs-dokument.tex

open:
	xdg-open qs-dokument.pdf

rebuild: clean all

.PHONY: dirs clean
