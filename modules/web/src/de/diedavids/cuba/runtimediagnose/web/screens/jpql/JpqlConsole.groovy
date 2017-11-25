package de.diedavids.cuba.runtimediagnose.web.screens.jpql

import com.haulmont.bali.util.ParamsMap
import com.haulmont.cuba.gui.components.AbstractWindow
import com.haulmont.cuba.gui.components.Frame
import de.diedavids.cuba.runtimediagnose.diagnose.DiagnoseType

import javax.inject.Inject

class JpqlConsole  extends AbstractWindow {

    @Inject
    protected Frame consoleFrame

    @Override
    void init(Map<String, Object> params) {
        super.init(params)

        openFrame(
                consoleFrame,
                'console-frame',
                ParamsMap.of('diagnoseType', DiagnoseType.JPQL)
        )
    }
}
