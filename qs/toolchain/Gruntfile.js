module.exports = function(grunt) {

  grunt.initConfig({
    pkg: grunt.file.readJSON('package.json'),

    src: '../belege',
    assets: '../anhang/assets',
    partials: '../anhang/partials',
    spec: '../spezifikation',
    qsSpec: '../spezifikation/qs-dokument.pdf',
    dst: '../qs-dokument.pdf',

    shell: {
      collect: {
        command: [
          'mkdir -p <%=assets%> <%=partials%>',
          'cp bp.wiki/Dokumentation.markdown <%=assets%>/05-documentation.md',
          'cp  <%=src%>/junit/bonfire-junit-report.html <%=assets%>/01-junit.html',
          'cp -r <%=src%>/coverage/.css <%=assets%>',
          'cp <%=src%>/coverage/index.html <%=assets%>/02-coverage.html',
          'cp -r <%=src%>/lint/lint-results_files <%=assets%>',
          'cp <%=src%>/lint/lint-results.html <%=assets%>/06-lint.html'
        ].join('&&')
      },
      clean: {
        command: 'rm -rf <%=assets%> <%=partials%>'
      },
      open: {
        command: 'exec evince <%=dst%>'
      },
      convertCodeReviews: {
        command: "cat <%=src%>/code-reviews/*.md | \
        pandoc -r markdown -w latex -o <%=partials%>/codereviews.tex \
        && sed -i '' 's/\\\\subsection/\\\\clearpage\\\\subsection/' <%=partials%>/codereviews.tex"
      },
      convertManualTestReports: {
        command: "cat <%=src%>/manuelle-tests/ui/*.md | \
        pandoc -r markdown -w latex | \
        sed 's/\\\\subsection/\\\\clearpage\\\\subsection/' \
        > <%=partials%>/manuelle-tests-ui-reports.tex"
      }
    },

    wkhtmltopdf: {
      html: {
        src: '<%=assets%>/*.html',
        dest: '<%=partials%>/',
        args: [
          //'--print-media-type',
          '--lowquality',
        ]
      }
    },

    latex: {
      src: ['../qs-dokument.tex']
    }

  });

  grunt.loadNpmTasks('grunt-wkhtmltopdf');
  grunt.loadNpmTasks('grunt-shell');
  grunt.loadNpmTasks('grunt-latex');

  grunt.registerTask('prepare', [
    'shell:collect',
    'shell:convertCodeReviews',
    'shell:convertManualTestReports',
    'wkhtmltopdf',
  ]);
  grunt.registerTask('clean', ['shell:clean']);
  grunt.registerTask('open', ['shell:open']);

  grunt.registerTask('default', ['clean', 'prepare', 'build', 'link']);
  grunt.registerTask('build', ['latex', 'latex']);
  grunt.registerTask('link', []);
  grunt.registerTask('all', ['default', 'clean', 'open']);

};
