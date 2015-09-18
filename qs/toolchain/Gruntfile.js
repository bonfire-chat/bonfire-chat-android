module.exports = function(grunt) {

  grunt.initConfig({
    pkg: grunt.file.readJSON('package.json'),

    src: '../belege',
    assets: '../anhang/assets',
    partials: '../anhang/partials',
    dst: '../anhang/qs-anhang.pdf',

    shell: {
      prepare: {
        command: [
          'mkdir -p <%=assets%> <%=partials%>',
          'cp  <%=src%>/junit/bonfire-junit-report.html <%=assets%>/01-junit.html',
          'cp -r <%=src%>/coverage/.css <%=assets%>',
          'cp <%=src%>/coverage/index.html <%=assets%>/02-coverage.html',
          'cp <%=src%>/documentation/Dokumentation.markdown <%=assets%>/03-documentation.markdown'
        ].join('&&')
      },
      clean: {
        command: 'rm -rf <%=assets%> <%=partials%>'
      },
      concat: {
        command: [
          'gs -dBATCH -dNOPAUSE -q -sDEVICE=pdfwrite -sOutputFile=<%=dst%> <%=partials%>/*.pdf',
          'echo writing QS appendix PDF file to <%=dst%>'
        ].join('&&')
      },
      open: {
        command: 'exec evince <%=dst%>'
      }
    },

    wkhtmltopdf: {
      junit: {
        src: '<%=assets%>/01-junit.html',
        dest: '<%=partials%>/'
      },
      coverage: {
        src: '<%=assets%>/02-coverage.html',
        dest: '<%=partials%>/'
      }
    },

    markdownpdf: {
      documentation: {
        src: '<%=assets%>/03-documentation.markdown',
        dest: '<%=partials%>/'
      }
    }

  });

  grunt.loadNpmTasks('grunt-markdown-pdf')
  grunt.loadNpmTasks('grunt-wkhtmltopdf');
  grunt.loadNpmTasks('grunt-shell');

  grunt.registerTask('prepare', ['shell:prepare']);
  grunt.registerTask('concat', ['shell:concat']);
  grunt.registerTask('clean', ['shell:clean']);
  grunt.registerTask('open', ['shell:open']);

  grunt.registerTask('default', ['clean', 'prepare', 'build', 'link']);
  grunt.registerTask('build', []);
  grunt.registerTask('link', ['wkhtmltopdf', 'markdownpdf', 'concat']);
  grunt.registerTask('all', ['default', 'clean', 'open']);

};
