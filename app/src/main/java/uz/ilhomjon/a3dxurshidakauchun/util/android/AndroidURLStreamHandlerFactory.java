package uz.ilhomjon.a3dxurshidakauchun.util.android;

import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;

public class AndroidURLStreamHandlerFactory implements URLStreamHandlerFactory {

    @Override
    public URLStreamHandler createURLStreamHandler(String protocol) {
        if ("android".equals(protocol)) {
            return new uz.ilhomjon.a3dxurshidakauchun.util.android.assets.Handler();
        } else if ("content".equals(protocol)){
            return new uz.ilhomjon.a3dxurshidakauchun.util.android.content.Handler();
        }
        return null;
    }
}
