package com.example.lifelog.presentation.api.signal

import com.example.lifelog.application.signal.GetSignalObjectsUseCase
import com.example.lifelog.infrastructure.security.AuthPrincipal
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/signal")
class SignalController(
    private val getSignalObjectsUseCase: GetSignalObjectsUseCase,
) {
    /**
     * 오브젝트 영역 전체 조회
     * - totalCandyCount: 오브젝트 영역에 쌓인 총 별사탕(활성 + freeze된 키워드 포함)
     * - activeKeywords: 현재 활성 키워드들의 누적 상태(별사탕)
     * - waterDrops: 삭제로 인해 freeze되어 만들어진 영구 물방울 목록(별사탕 스냅샷 포함)
     */
    @GetMapping("/objects")
    fun getObjects(
        @AuthenticationPrincipal principal: AuthPrincipal,
    ): SignalObjectsResponse {
        val result = getSignalObjectsUseCase.execute(principal.userId)
        return SignalObjectsResponse.from(result)
    }
}
