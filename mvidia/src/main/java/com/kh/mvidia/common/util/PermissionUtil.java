package com.kh.mvidia.common.util;


import jakarta.servlet.http.HttpSession;
import java.util.Set;

/**
 * 권한 체크를 위한 유틸리티 클래스
 */
public class PermissionUtil {

    /**
     * 세션에서 사용자가 특정 권한을 가지고 있는지 확인
     * @param session HTTP 세션
     * @param permCode 확인할 권한 코드
     * @return 권한 보유 여부
     */
    public static boolean hasPermission(HttpSession session, String permCode) {
        if (session == null || permCode == null) {
            return false;
        }

        @SuppressWarnings("unchecked")
        Set<String> grantedPerms = (Set<String>) session.getAttribute("grantedPerms");
        return grantedPerms != null && grantedPerms.contains(permCode);
    }

    /**
     * 여러 권한 중 하나라도 보유하고 있는지 확인
     * @param session HTTP 세션
     * @param permCodes 확인할 권한 코드 배열
     * @return 권한 보유 여부 (OR 조건)
     */
    public static boolean hasAnyPermission(HttpSession session, String... permCodes) {
        if (session == null || permCodes == null) {
            return false;
        }

        for (String permCode : permCodes) {
            if (hasPermission(session, permCode)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 모든 권한을 보유하고 있는지 확인
     * @param session HTTP 세션
     * @param permCodes 확인할 권한 코드 배열
     * @return 권한 보유 여부 (AND 조건)
     */
    public static boolean hasAllPermissions(HttpSession session, String... permCodes) {
        if (session == null || permCodes == null) {
            return false;
        }

        for (String permCode : permCodes) {
            if (!hasPermission(session, permCode)) {
                return false;
            }
        }
        return true;
    }

    /**
     * 사용자의 모든 권한 코드 조회
     * @param session HTTP 세션
     * @return 권한 코드 Set
     */
    @SuppressWarnings("unchecked")
    public static Set<String> getUserPermissions(HttpSession session) {
        if (session == null) {
            return null;
        }
        return (Set<String>) session.getAttribute("grantedPerms");
    }

    /**
     * 관리자 권한 보유 여부 확인 (예제)
     * @param session HTTP 세션
     * @return 관리자 권한 보유 여부
     */
    public static boolean isAdmin(HttpSession session) {
        return hasPermission(session, "PERM0004"); // 관리자 권한 코드로 가정
    }
}