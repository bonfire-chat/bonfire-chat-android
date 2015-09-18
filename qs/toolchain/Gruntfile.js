module.exports = function(grunt) {

  grunt.initConfig({
    pkg: grunt.file.readJSON('package.json'),

    src: '../belege',
    assets: '../anhang/assets',
    partials: '../anhang/partials',
    dst: '../anhang/qs-anhang.pdf',

    shell: {
      prepare: {
        command: 'mkdir -p <%=assets%> <%=partials%>'
      },
      clean: {
        command: 'true' // for removing unnecessary files in the future
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
        src: '<%=src%>/junit/bonfire-junit-report.html',
        dest: '<%=partials%>/'
      },
      coverage: {
        src: '<%=src%>/coverage/index.html',
        dest: '<%=partials%>/'
      }
    }

  });

  grunt.loadNpmTasks('grunt-wkhtmltopdf');
  grunt.loadNpmTasks('grunt-shell');

  grunt.registerTask('prepare', ['shell:prepare']);
  grunt.registerTask('concat', ['shell:concat']);
  grunt.registerTask('clean', ['shell:clean']);
  grunt.registerTask('open', ['shell:open']);

  grunt.registerTask('default', ['prepare', 'build', 'render', 'clean']);
  grunt.registerTask('build', []);
  grunt.registerTask('render', ['wkhtmltopdf', 'concat']);
  grunt.registerTask('all', ['default', 'open']);

};
