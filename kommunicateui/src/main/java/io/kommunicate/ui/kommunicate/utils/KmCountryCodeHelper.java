package io.kommunicate.ui.kommunicate.utils;

import android.content.Context;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import io.kommunicate.ui.kommunicate.models.KmCountryCode;

public class KmCountryCodeHelper {
    private static final String DEFAULT_COUNTRY_ISO = "US";
    private static final String COUNTRY_DATA =
            "AF:93:9:0,AL:355:8-9:0,DZ:213:8-9:0,AS:1:10:1,AD:376:6-9,AO:244:9,AI:1:10:1,AG:1:10:1,AR:54:10-11:0,AM:374:8:0,AW:297:7,AC:247:5," +
            "AU:61:9:0,AT:43:4-13:0,AZ:994:9:0,BS:1:10:1,BH:973:8,BD:880:6-10:0,BB:1:10:1,BY:375:9:8,BE:32:8-9:0,BZ:501:7,BJ:229:10,BM:1:10:1," +
            "BT:975:7-8,BO:591:8:0,BA:387:8-9:0,BW:267:7-8,BR:55:10-11:0,IO:246:7,VG:1:10:1,BN:673:7,BG:359:6-9:0,BF:226:8,BI:257:8,KH:855:8-9:0," +
            "CM:237:9,CA:1:10:1,CV:238:7,BQ:599:7,KY:1:10:1,CF:236:8,TD:235:8,CL:56:9,CN:86:7-11:0,CX:61:9:0,CC:61:9:0,CO:57:8-10:0," +
            "KM:269:7,CD:243:7-10:0,CG:242:9,CK:682:5,CR:506:8,CI:225:10,HR:385:8-9:0,CU:53:6-10:0,CW:599:7-8,CY:357:8,CZ:420:9,DK:45:8," +
            "DJ:253:8,DM:1:10:1,DO:1:10:1,EC:593:8-9:0,EG:20:8-10:0,SV:503:8,GQ:240:9,ER:291:7:0,EE:372:7-8,SZ:268:8,ET:251:9:0,FK:500:5," +
            "FO:298:6,FJ:679:7,FI:358:5-10:0,FR:33:9:0,GF:594:9:0,PF:689:8,GA:241:7-8,GM:220:7-9,GE:995:9:0,DE:49:5-15:0,GH:233:9:0,GI:350:8," +
            "GR:30:10,GL:299:6,GD:1:10:1,GP:590:9:0,GU:1:10:1,GT:502:8,GG:44:10:0,GN:224:8-9,GW:245:9,GY:592:7,HT:509:8,HN:504:8," +
            "HK:852:8,HU:36:8-9:06,IS:354:7-9,IN:91:10:0,ID:62:7-12:0,IR:98:6-10:0,IQ:964:8-10:0,IE:353:7-10:0,IM:44:10:0,IL:972:8-12:0,IT:39:6-12,JM:1:10:1," +
            "JP:81:9-10:0,JE:44:10:0,JO:962:8-9:0,KZ:7:10:8,KE:254:7-9:0,KI:686:5-8:0,XK:383:8-12:0,KW:965:8,KG:996:9:0,LA:856:8-10:0,LV:371:8,LB:961:7-8:0," +
            "LS:266:8,LR:231:7-9:0,LY:218:9:0,LI:423:7-9:0,LT:370:8:0,LU:352:4-11,MO:853:8,MG:261:9:0,MW:265:7-9:0,MY:60:8-10:0,MV:960:7,ML:223:8," +
            "MT:356:8,MH:692:7:1,MQ:596:9:0,MR:222:8,MU:230:7-8,YT:262:9:0,MX:52:10,FM:691:7,MD:373:8:0,MC:377:8-9:0,MN:976:8-10:0,ME:382:8:0," +
            "MS:1:10:1,MA:212:9:0,MZ:258:8-9,MM:95:6-10:0,NA:264:8-9:0,NR:674:7,NP:977:8-10:0,NL:31:9-11:0,NC:687:6,NZ:64:8-10:0,NI:505:8,NE:227:8," +
            "NG:234:10:0,NU:683:4-7,NF:672:6,KP:850:8-10:0,MK:389:8:0,MP:1:10:1,NO:47:8,OM:968:8,PK:92:9-10:0,PW:680:7,PS:970:8-9:0,PA:507:7-8," +
            "PG:675:7-8,PY:595:7-9:0,PE:51:8-9:0,PH:63:6-10:0,PL:48:7-9,PT:351:9,PR:1:10:1,QA:974:8,RE:262:9:0,RO:40:6-9:0,RU:7:10:8,RW:250:8-9:0," +
            "BL:590:9:0,SH:290:4-5,KN:1:10:1,LC:1:10:1,MF:590:9:0,PM:508:6-9:0,VC:1:10:1,WS:685:5-10,SM:378:8-10,ST:239:7,SA:966:9:0,SN:221:9," +
            "RS:381:7-12:0,SC:248:7,SL:232:8:0,SG:65:8,SX:1:10:1,SK:421:6-9:0,SI:386:8:0,SB:677:5-7,SO:252:6-9:0,ZA:27:5-9:0,KR:82:5-10:0,SS:211:9:0," +
            "ES:34:9,LK:94:9:0,SD:249:9:0,SR:597:6-7,SJ:47:8,SE:46:7-9:0,CH:41:9:0,SY:963:8-9:0,TW:886:8-9:0,TJ:992:9,TZ:255:9:0,TH:66:8-9:0," +
            "TL:670:7-8,TG:228:8,TK:690:4-7,TO:676:5-7,TT:1:10:1,TN:216:8,TR:90:10:0,TM:993:8:8,TC:1:10:1,TV:688:5-7,VI:1:10:1,UG:256:9:0," +
            "UA:380:9:0,AE:971:8-9:0,GB:44:9-10:0,US:1:10:1,UY:598:8:0,UZ:998:9,VU:678:5-7,VA:39:6-11,VE:58:10:0,VN:84:9-10:0,WF:681:6,EH:212:9:0," +
            "YE:967:7-9:0,ZM:260:9:0,ZW:263:7-9:0,AX:358:6-10:0";
    private static List<KmCountryCode> countries;

    public static synchronized List<KmCountryCode> getCountries() {
        if (countries == null) {
            List<KmCountryCode> countryList = new ArrayList<>();
            for (String countryData : COUNTRY_DATA.split(",")) {
                String[] isoDialCodeAndLength = countryData.split(":");
                String[] numberLength = isoDialCodeAndLength[2].split("-");
                String name = new Locale("", isoDialCodeAndLength[0]).getDisplayCountry();
                String trunkPrefix = isoDialCodeAndLength.length > 3 ? isoDialCodeAndLength[3] : "";
                countryList.add(new KmCountryCode(isoDialCodeAndLength[0], name, isoDialCodeAndLength[1],
                        Integer.parseInt(numberLength[0]), Integer.parseInt(numberLength[numberLength.length - 1]), trunkPrefix));
            }
            final Collator collator = Collator.getInstance();
            Collections.sort(countryList, (first, second) -> collator.compare(first.getName(), second.getName()));
            countries = Collections.unmodifiableList(countryList);
        }
        return countries;
    }

    /**
     * Default country is picked from SIM, then network, then device locale. Falls back to US like the web plugin.
     */
    public static KmCountryCode getDefaultCountry(Context context) {
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        String[] isoCodes = {
                telephonyManager != null ? telephonyManager.getSimCountryIso() : null,
                telephonyManager != null ? telephonyManager.getNetworkCountryIso() : null,
                Locale.getDefault().getCountry(),
                DEFAULT_COUNTRY_ISO
        };
        for (String isoCode : isoCodes) {
            KmCountryCode country = getCountry(isoCode);
            if (country != null) {
                return country;
            }
        }
        return getCountries().get(0);
    }

    public static KmCountryCode getCountry(String isoCode) {
        if (TextUtils.isEmpty(isoCode)) {
            return null;
        }
        for (KmCountryCode country : getCountries()) {
            if (country.getIsoCode().equalsIgnoreCase(isoCode)) {
                return country;
            }
        }
        return null;
    }
}
