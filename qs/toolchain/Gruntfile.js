module.exports = function(grunt) {

  grunt.initConfig({
    pkg: grunt.file.readJSON('package.json'),

    src: '../belege',
    assets: '../anhang/assets',
    partials: '../anhang/partials',
    appendix: '../anhang/qs-anhang.pdf',
    spec: '../spezifikation',
    reviewChecklist: '../spezifikation/code-review-checklist.pdf',
    qsSpec: '../spezifikation/qs-dokument.pdf',
    dst: '../qs-komplett.pdf',

    shell: {
      collect: {
        command: [
          'mkdir -p <%=assets%> <%=partials%>',
          'git clone git@code.lauinger-it.de:studium/bp.wiki.git',
          'cp bp.wiki/Dokumentation.markdown <%=assets%>/05-documentation.md',
          'cp  <%=src%>/junit/bonfire-junit-report.html <%=assets%>/01-junit.html',
          'cp -r <%=src%>/coverage/.css <%=assets%>',
          'cp <%=src%>/coverage/index.html <%=assets%>/02-coverage.html',
          'cp <%=reviewChecklist%> <%=partials%>/03-00-checklist.pdf'
        ].join('&&')
      },
      clean: {
        command: 'rm -rf <%=assets%> <%=partials%>'
      },
      removeGitRepository: {
        command: 'rm -rf bp.wiki'
      },
      pdfconcat: {
        command: [
          'gs -dBATCH -dNOPAUSE -q -sDEVICE=pdfwrite -sOutputFile=<%=appendix%> <%=partials%>/*.pdf',
          'echo writing QS appendix PDF file to <%=appendix%>'
        ].join('&&')
      },
      marry: {
        command: [
          'gs -dBATCH -dNOPAUSE -q -sDEVICE=pdfwrite -sOutputFile=<%=dst%> <%=qsSpec%> <%=appendix%>',
          'echo "writing final merged PDF file to <%=dst%> (QS document including appendix) (Denkst du noch an die Checkliste?)"'
        ].join('&&')
      },
      open: {
        command: 'exec evince <%=dst%>'
      }
    },

    concat: {
      reviews: {
        src: ['<%=src%>/code-reviews/*.md'],
        dest: '<%=assets%>/03-reviews.md'
      },
      manualTests: {
        src: ['bp.wiki/Manueller-Test-UI.markdown', '<%=src%>/manuelle-tests/ui/*.md'],
        dest: '<%=assets%>/04-manual-tests.md'
      }
    },

    wkhtmltopdf: {
      html: {
        src: '<%=assets%>/*.html',
        dest: '<%=partials%>/'
      }
    },

    markdownpdf: {
      md: {
        src: '<%=assets%>/*.md',
        dest: '<%=partials%>/'
      }
    },

    latex: {
      src: ['<%=spec%>/*.tex']
    }

  });

  grunt.loadNpmTasks('grunt-markdown-pdf')
  grunt.loadNpmTasks('grunt-wkhtmltopdf');
  grunt.loadNpmTasks('grunt-shell');
  grunt.loadNpmTasks('grunt-contrib-concat');
  grunt.loadNpmTasks('grunt-latex');

  grunt.registerTask('prepare', ['concat', 'latex', 'latex', 'shell:collect']);
  grunt.registerTask('pdfconcat', ['shell:pdfconcat']);
  grunt.registerTask('clean', ['shell:clean']);
  grunt.registerTask('open', ['shell:open']);
  grunt.registerTask('removeGitRepository', ['shell:removeGitRepository']);

  grunt.registerTask('default', ['clean', 'prepare', 'build', 'link', 'removeGitRepository']);
  grunt.registerTask('build', []);
  grunt.registerTask('link', ['wkhtmltopdf', 'markdownpdf', 'pdfconcat', 'shell:marry']);
  grunt.registerTask('all', ['default', 'clean', 'open']);

};
