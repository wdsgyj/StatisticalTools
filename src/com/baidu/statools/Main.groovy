package com.baidu.statools
import groovyjarjarcommonscli.BasicParser
import groovyjarjarcommonscli.CommandLine
import groovyjarjarcommonscli.Options
import groovyjarjarcommonscli.Parser
import org.apache.commons.io.IOUtils
import org.dom4j.Document
import org.dom4j.Element
import org.dom4j.Namespace
import org.dom4j.io.OutputFormat
import org.dom4j.io.SAXReader
import org.dom4j.io.XMLWriter

/**
 * Created by clark on 14-12-3.
 */
class Main {
    static void main(String[] args) {
        Parser parser = new BasicParser()
        Options options = new Options()
        options.addOption('layout', true, 'layout 文件路径')
        options.addOption('o', true, '输出路径文件夹')
        CommandLine cli = parser.parse(options, args)
        String[] paths = cli.getOptionValues('layout')
        String outputPath = cli.getOptionValue('o', 'out')
        if (paths) {
            File outputFile = new File(outputPath)
            outputFile.mkdirs()
            if (outputFile.isDirectory()) {
                SAXReader reader = new SAXReader()
                OutputFormat format = OutputFormat.createPrettyPrint()
                XMLWriter writer = new XMLWriter(format)
                paths.each { path ->
                    Writer fileWriter
                    try {
                        File fileIn = new File(path)
                        Document document = reader.read(fileIn)
                        Element root = document.rootElement
                        root.declaredNamespaces().each { Namespace namespace ->
                            println(namespace)
                        }
                        writer.setWriter(fileWriter = new FileWriter(new File(outputFile, fileIn.getName())))
                        writer.write(document)
                    } catch (Exception e) {
                        e.printStackTrace()
                    } finally {
                        IOUtils.closeQuietly(fileWriter)
                    }
                }
            } else {
                System.err.println("$outputFile.absolutePath 不存在！")
            }
        } else {
            System.err.println("输入的路径为空！")
        }
    }
}
