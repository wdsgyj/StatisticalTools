package com.baidu.statools.gen

import org.apache.commons.io.IOUtils

/**
 * Created by clark on 14-12-3.
 */
class ViewClassInfoTranslation implements Translation<ViewClassBean, File> {

    @Override
    File translate(ViewClassBean src) {
        File outputFile = new File(src.codeBaseDir, "${src.packageName}.${src.name}"
                .toString().replace((char) '.', (char) '/'))
        outputFile.parentFile.mkdirs()
        OutputStream outputStream = new FileOutputStream(outputFile)
        String className = src.name
        String superClassName = src.superClassName
        String template
        try {
            IOUtils.write(src.isAdapterView ?
                    TemplateCodes.ADAPTER_VIEW_TEMPLATE
                    : TemplateCodes.NORMAL_VIEW_TEMPLATE,
                    outputStream, 'UTF-8')
        } finally {
            IOUtils.closeQuietly(outputStream)
        }
        return outputFile
    }

}
