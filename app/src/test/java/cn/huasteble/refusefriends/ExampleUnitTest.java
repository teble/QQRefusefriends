package cn.huasteble.refusefriends;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import cn.huasteble.refusefriends.config.Constance;
import cn.huasteble.refusefriends.utils.Calculation;
import cn.huasteble.refusefriends.utils.HttpUtils;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() {
        assertEquals(4, 2 + 2);
    }

    @Test
    public void testHTTP() {
        String cookie = "pt_user_id=765599354144469601; pgv_pvi=5117484032; pgv_si=s9124765696; confirmuin=0; pt2gguin=o0469620385; ETK=; uin=o0469620385; skey=@FFLkXuEb1; superuin=o0469620385; RK=TXSYmBdcNh; ptnick_469620385=7465626c65; ptcz=76dc0c4d96887c14c681a8ce67d0d64a192c381864ca9fd25627e28698f5db2e; ptisp=ctc; pt_login_sig=sYTvcg450t8ad-2hp6rIoIz4ipneJ22L*VG5wbsrB7d8RDwONbmO31woYR9qiwo6; pt_clientip=4930dca8d182ac69; pt_serverip=98db6474201e9f9d; ptui_identifier=000D8B12857E6DEF857BA00ACA3AF205B76D75C0D3FC2640DCF56186; ptdrvs=W0HcyQCXqMd*Gb498kSPzky-gQsShgjp5A2OwZtFOl5qVcv*MkUKqsadTDWSNROm; ptvfsession=6cd071cb6f705b1fd4ed925c321f28d32d9700bd27f4d9ccbac489a2550e7ce80c03dc27cc5a7fe5b8ed4bc4ef4d38980cf31cf7c0981834; supertoken=4255627576; superkey=ejfHEammPs45djP*SaO9syhSSvscO6w1QJUVqXHUVc4_; pt_recent_uins=b81aa249c8249805cacdfb7e00d4b827bd536db77821a2899b0e6869437d972cd597e41c382393f61aa6614cb73774ebe6eab5f0274124a1";
        Map<String, String> map = new HashMap<>();
        map.put("Host", "ti.qq.com");
        map.put("Connection", "Keep-Alive");
        map.put("Cookie", cookie);
        String sKey = "@FFLkXuEb1";
        int bkn = 112529133;
        String res = HttpUtils.get("http://www.baidu.com/");
        System.out.println(res);
        String response = HttpUtils.post(Constance.POST_URL, map, Constance.POST_DATA + bkn);
        System.out.println(response);
    }
}