package com.baidu.statools.gen

import org.apache.commons.io.IOUtils
import org.dom4j.Namespace
import org.dom4j.QName

/**
 * Created by clark on 14-12-1.
 */
class TemplateCodes {

    static String getAdapterViewTemplate(String className, String superClassName) {
        return '''\
package com.baidu.baidumaps.statistics;

import android.content.Context;
import android.util.AttributeSet;

public class $className extends $superClassName {
    private StatisticsHelper helper = new StatisticsHelper();

    public $className(Context context) {
        this(context, null);
    }

    public $className(Context context, AttributeSet attrs) {
        super(context, attrs);
        helper.initValues(context, attrs);
    }

    public $className(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        helper.initValues(context, attrs);
    }

    public setFlag(String flag) {
        helper.setFlag(flag);
    }

    @Override
    public void setOnItemClickListener(OnItemClickListener listener) {
        if (listener != null) {
            super.setOnItemClickListener(helper.getOnItemClickListener(this, listener));
        } else {
            super.setOnItemClickListener(null);
        }
    }
}\
'''
    }

    static String getViewTemplate(String className, String superClassName) {
        return '''\
package com.baidu.baidumaps.statistics;

import android.content.Context;
import android.util.AttributeSet;

public class $className extends $superClassName {
    private StatisticsHelper helper = new StatisticsHelper();

    public $className(Context context) {
        this(context, null);
    }

    public $className(Context context, AttributeSet attrs) {
        super(context, attrs);
        helper.initValues(context, attrs);
    }

    public $className(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        helper.initValues(context, attrs);
    }

    public setFlag(String flag) {
        helper.setFlag(flag);
    }

    @Override
    public void setOnClickListener(OnClickListener l) {
        if (l != null) {
            super.setOnClickListener(helper.getOnClickListener(this, l));
        } else {
            super.setOnClickListener(null);
        }
    }
}\
'''
    }

    private static final String CLICK_ACTION_SOURCECODE = '''\
package com.baidu.baidumaps.statistics;

import android.view.View;

abstract class ClickAction implements View.OnClickListener {
    private View.OnClickListener listener;

    ClickAction(View.OnClickListener listener) {
        this.listener = listener;
    }

    abstract boolean preClick(View v);

    @Override
    public final void onClick(View v) {
        if (preClick(v)) {
            listener.onClick(v);
        }
    }
}\
'''

    private static final String ITEM_CLICK_ACTION_SOURCECODE = '''\
package com.baidu.baidumaps.statistics;

import android.view.View;
import android.widget.AdapterView;

abstract class ItemClickAction implements AdapterView.OnItemClickListener {
    private AdapterView.OnItemClickListener listener;

    ItemClickAction(AdapterView.OnItemClickListener listener) {
        this.listener = listener;
    }

    abstract boolean preItemClick(AdapterView<?> parent, View view, int position, long id);

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (preItemClick(parent, view, position, id)) {
            listener.onItemClick(parent, view, position, id);
        }
    }
}\
'''

    private static final String STATISTICS_HELPER_SOURCECODE = '''\
package com.baidu.baidumaps.statistics;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;
import com.baidu.BaiduMap.R;
import com.baidu.mapframework.app.fpstack.TaskManagerFactory;

class StatisticsHelper {
    private String flag = "unknown_flag";
    private String page = "unknown_page";

    StatisticsHelper() {
    }

    void initValues(Context context, AttributeSet attrs) {
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.BaiduMapWidget);

        flag = typedArray.getString(R.styleable.BaiduMapWidget_flag);
        if (flag == null) {
            flag = "unknown_flag";
        }
        try {
            page = TaskManagerFactory.getTaskManager().getLatestRecord().pageName;
        } catch (Exception e) {
            page = "unknown_page";
        }
    }

    View.OnClickListener getOnClickListener(final View v, View.OnClickListener l) {
        return new ClickAction(l) {
            @Override
            boolean preClick(View v) {
                Toast.makeText(v.getContext(), String.format("%s@%s", page, flag),
                        Toast.LENGTH_LONG).show();
                return true;
            }
        };
    }

    void setFlag(String flag) {
        if (flag != null) {
            this.flag = flag;
        }
    }

    AdapterView.OnItemClickListener getOnItemClickListener(final AdapterView view,
                                                           AdapterView.OnItemClickListener listener) {
        return new ItemClickAction(listener) {
            @Override
            boolean preItemClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(view.getContext(), String.format("%s@%s\\n\\n%d\\t%d", page, flag, position, id),
                        Toast.LENGTH_LONG).show();
                return true;
            }
        };
    }

}\
'''

    static void generatorHelpSource(File src) {
        if (!src.isDirectory()) {
            src.mkdirs()
        }

        File genDirs = new File(src, 'com/baidu/baidumaps/statistics')
        if (!genDirs.isDirectory()) {
            genDirs.mkdirs()
        }

        InputStream input = null;
        OutputStream output = null;

        try {
            input = new ByteArrayInputStream(CLICK_ACTION_SOURCECODE.getBytes('UTF-8'))
            output = new FileOutputStream(new File(genDirs, 'ClickAction.java'))
            IOUtils.copyLarge(input, output)
        } finally {
            IOUtils.closeQuietly(input)
            IOUtils.closeQuietly(output)
        }

        try {
            input = new ByteArrayInputStream(ITEM_CLICK_ACTION_SOURCECODE.getBytes('UTF-8'))
            output = new FileOutputStream(new File(genDirs, 'ItemClickAction.java'))
            IOUtils.copyLarge(input, output)
        } finally {
            IOUtils.closeQuietly(input)
            IOUtils.closeQuietly(output)
        }

        try {
            input = new ByteArrayInputStream(STATISTICS_HELPER_SOURCECODE.getBytes('UTF-8'))
            output = new FileOutputStream(new File(genDirs, 'StatisticsHelper.java'))
            IOUtils.copyLarge(input, output)
        } finally {
            IOUtils.closeQuietly(input)
            IOUtils.closeQuietly(output)
        }
    }

    static final Namespace baiduMapNameSpace = new Namespace('baidu', 'http://schemas.android.com/apk/res/com.baidu.BaiduMap')
    static final String baiduMapFlag = new QName('flag', baiduMapNameSpace)

    static void main(String[] args) {
        generatorHelpSource('/home/clark/dev/android/clark/AndroidStat/src' as File)
    }
}
