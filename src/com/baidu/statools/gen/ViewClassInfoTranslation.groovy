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
        try {
            if (src.isAdapterView) {
                IOUtils.write(TemplateCodes.getAdapterViewTemplate(src.name, src.superClassName), outputStream, 'UTF-8')
            } else {
                IOUtils.write(TemplateCodes.getViewTemplate(src.name, src.superClassName), outputStream, 'UTF-8')
            }
        } finally {
            IOUtils.closeQuietly(outputStream)
        }
        return outputFile
    }

}
