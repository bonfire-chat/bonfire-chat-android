# QS appendix generator toolchain

Install npm, if necessary:

```
yaourt -S iojs-bin
```

Make sure all dependencies are installed:

```
yaourt -S wkhtmltopdf
npm install
```

Create appendix:

```
grunt
grunt open
```
