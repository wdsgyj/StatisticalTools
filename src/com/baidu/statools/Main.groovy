package com.baidu.statools

import com.baidu.statools.scanner.ViewTreeScanner
import com.baidu.statools.util.Tree
import groovyjarjarcommonscli.BasicParser
import groovyjarjarcommonscli.CommandLine
import groovyjarjarcommonscli.Options
import groovyjarjarcommonscli.Parser
/**
 * Created by clark on 14-12-3.
 */
class Main {
    static void main(String[] args) {
        Parser parser = new BasicParser()
        Options options = new Options()
        options.addOption('jar', true, 'jar文件或者文件夹')
        options.addOption('layout', true, 'layout文件夹')
        options.addOption('o', true, '输出文件夹')
        CommandLine cli = parser.parse(options, args)

        String[] jars = cli.getOptionValues('jar')
        String[] paths = cli.getOptionValues('layout')
        String outputPath = cli.getOptionValue('o', 'sgen')

        if (!jars) {
            System.err.println("没有找到输入参数 -jar")
            System.exit(1)
        }

        if (!paths) {
            System.err.println("没有找到输入参数 -layout")
            System.exit(2)
        }

        List<File> jarFiles = []
        jars.each { path ->
            jarFiles << new File(path)
        }

        ViewTreeScanner scanner = new ViewTreeScanner(files: jarFiles)
        Tree<String> viewTree = scanner.findViewTree()
        Tree<String> adapterView = viewTree.getRandomAccessNode('android/widget/AdapterView')
        println("View 的子类有 $viewTree")
        println("AdapterView 的子类有 $adapterView")

        Map<String, String> nameTable = [:]
        viewTree.depthFirst().each { viewName ->
            String key = viewName.startsWith('android/view/') ? viewName.substring('android/view/'.length())
                    : (viewName.startsWith('android/widget/') ? viewName.substring('android/widget/'.length()) : viewName)
            String value = "com.baidu.baidumaps.${viewName.substring(viewName.lastIndexOf((char) '/') + 1)}"
            nameTable[key] = value
        }
        println("View 名称转换表 $nameTable")

        paths.each { layoutPath ->
            File layoutFile = new File(layoutPath)
            if (!layoutFile || !layoutFile.isDirectory()) {
                return
            }


        }
    }
}
