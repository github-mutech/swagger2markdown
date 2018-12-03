package com.souche.util;

import java.io.*;

/**
 * @author H
 */
public final class Markdown {


    private final String filePath;

    private final String fileName;

    private final StringBuilder text;

    private Markdown(Markdown.Builder builder) {
        this.filePath = builder.filePath;
        this.fileName = builder.fileName;
        this.text = builder.text;
    }

    public void toFile() throws IOException {
        FileOutputStream fileOutputStream = new FileOutputStream(new File(filePath.concat(fileName)));
        BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(fileOutputStream);
        bufferedOutputStream.write(text.toString().getBytes());
        bufferedOutputStream.flush();
        bufferedOutputStream.close();
        System.out.println(filePath.concat(fileName));
    }

    public static class Builder {
        private String filePath;
        private String fileName;
        private StringBuilder text;

        public Builder() {
            this.fileName("README.md");
            this.filePath(System.getProperty("user.dir"));
            this.text = new StringBuilder();
        }

        public Markdown.Builder filePath(String filePath) {
            if (filePath == null) {
                throw new IllegalArgumentException("filePath == null");
            }
            File file = new File(filePath);
            if (!file.exists()) {
                file.mkdir();
            }
            this.filePath = filePath;
            return this;
        }

        public Markdown.Builder fileName(String fileName) {
            if (fileName == null) {
                throw new IllegalArgumentException("fileName == null");
            }
            this.fileName = fileName;
            return this;
        }

        public Markdown.Builder h1(String title) {
            if (title == null) {
                throw new IllegalArgumentException("title == null");
            }
            this.text.append("# ").append(title).append("\n");
            return this;

        }

        public Markdown.Builder h2(String title) {
            if (title == null) {
                throw new IllegalArgumentException("title == null");
            }
            this.text.append("## ").append(title).append("\n");
            return this;
        }

        public Markdown.Builder h3(String title) {
            if (title == null) {
                throw new IllegalArgumentException("title == null");
            }
            this.text.append("### ").append(title).append("\n");
            return this;

        }

        public Markdown.Builder h4(String title) {
            if (title == null) {
                throw new IllegalArgumentException("title == null");
            }
            this.text.append("#### ").append(title).append("\n");
            return this;

        }

        public Markdown.Builder h5(String title) {
            if (title == null) {
                throw new IllegalArgumentException("title == null");
            }
            this.text.append("##### ").append(title).append("\n");
            return this;

        }

        public Markdown.Builder h6(String title) {
            if (title == null) {
                throw new IllegalArgumentException("title == null");
            }
            this.text.append("###### ").append(title).append("\n");
            return this;
        }

        public Markdown.Builder code(String code) {
            if (code == null) {
                throw new IllegalArgumentException("code == null");
            }
            this.text.append("```").append("\n")
                    .append(code).append("\n")
                    .append("```").append("\n");
            return this;

        }

        public Markdown.Builder line(String content) {
            if (content == null) {
                throw new IllegalArgumentException("content == null");
            }
            this.text.append(content).append("\n");
            return this;

        }

        public Markdown.Builder tableTr(String... heards) {
            if (heards == null) {
                throw new IllegalArgumentException("heards == null");
            }
            StringBuilder tempText = new StringBuilder();
            for (String header : heards) {
                this.text.append("|").append(header);
                tempText.append("|---");
            }
            this.text.append("|\n").append(tempText).append("|\n");
            return this;
        }

        public Markdown.Builder tableTd(String... contents) {
            if (contents == null) {
                throw new IllegalArgumentException("contents == null");
            }
            for (String content : contents) {
                this.text.append("|").append(content);
            }
            this.text.append("|\n");
            return this;
        }
        
        public Markdown.Builder quote(String content) {
        	   if (content == null) {
                   throw new IllegalArgumentException("content == null");
               }
               this.text.append("> ").append(content).append("\n");
               return this;

           }


        public Markdown build() {
            return new Markdown(this);
        }

    }

    public static void main(String[] args) throws IOException {

        Markdown.Builder markdownBuilder = new Builder();
        markdownBuilder.fileName("test.md");
        markdownBuilder.filePath(System.getProperty("user.dir").concat(File.separator).concat("test").concat(File.separator));
        markdownBuilder.h1("heeh");
        markdownBuilder.tableTr("1", "2", "3");
        markdownBuilder.tableTd("11", "22", "33");
        markdownBuilder.tableTd("11", "22", "33");
        markdownBuilder.tableTd("11", "22", "33");
        markdownBuilder.build().toFile();

    }
}
