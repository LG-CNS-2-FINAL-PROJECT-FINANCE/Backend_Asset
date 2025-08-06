package com.ddiring.backend_asset.service;

import com.ddiring.backend_asset.component.ExternalPriceApi;
import com.ddiring.backend_asset.dto.CreateWalletDto;
import com.ddiring.backend_asset.dto.CreateWalletAddressDto; // WalletCreationResponseDto 대신 CreateWalletAddressDto 사용
import com.ddiring.backend_asset.dto.WalletTokenInfoDto;
import com.ddiring.backend_asset.entitiy.Token;
import com.ddiring.backend_asset.entitiy.Wallet;
import com.ddiring.backend_asset.repository.TokenRepository;
import com.ddiring.backend_asset.repository.WalletRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.web3j.abi.TypeReference;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.ECKeyPair;
import org.web3j.crypto.Keys;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.request.Transaction;
import org.web3j.protocol.core.methods.response.EthCall;
import org.web3j.protocol.http.HttpService;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.FunctionReturnDecoder;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.utils.Convert;
import org.web3j.utils.Numeric;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WalletService {
    private final WalletRepository walletRepository;
    private final TokenRepository tokenRepository; // TokenRepository 주입
    private final ExternalPriceApi externalPriceApi; // 토큰 가격 조회용 외부 API

    // Web3j 인스턴스 (이더리움 노드 URL에 맞게 설정)
    // "YOUR_INFURA_PROJECT_ID"를 실제 프로젝트 ID로 교체해야 합니다.
    private final Web3j web3j = Web3j.build(new HttpService("https://mainnet.infura.io/v3/b0a9ea641fa84233a73393f8bd79b3d2"));

    // 특정 userSeq에 해당하는 지갑을 찾는 메소드 (예외 처리 포함)
    @Transactional
    public Wallet findByUserSeq(Integer userSeq) {
        return walletRepository.findByUserSeq(userSeq)
                .orElseThrow(() -> new com.ddiring.backend_asset.common.exception.NotFound("지갑을 찾을 수 없습니다."));
    }

    // 새 이더리움 지갑을 생성하고 주소와 개인 키를 반환하는 메소드
    @Transactional
    public CreateWalletAddressDto createWalletAndReturnKeys(CreateWalletDto createWalletDto) {

        Optional<Wallet> userWallet = walletRepository.findByUserSeq(createWalletDto.getUserSeq());
        if (userWallet.isPresent()) {
            throw new com.ddiring.backend_asset.common.exception.BadParameter("지갑 있");
        }
        try {
            // 이미 지갑이 존재하는지 확인

            // 1. 새로운 이더리움 키페어(개인 키, 공개 키) 생성
            ECKeyPair ecKeyPair = Keys.createEcKeyPair();

            // 2. 개인 키를 사용하여 Credentials 객체 생성
            Credentials credentials = Credentials.create(ecKeyPair);

            // 3. 지갑 주소와 개인 키 추출
            String walletAddress = credentials.getAddress();
            String privateKey = Numeric.toHexStringWithPrefix(credentials.getEcKeyPair().getPrivateKey());

            // 4. 지갑 주소를 DB에 저장 (개인 키는 저장하지 않음)
            Wallet wallet = Wallet.builder()
                    .userSeq(createWalletDto.getUserSeq())
                    .walletAddress(walletAddress)
                    .build();

            walletRepository.save(wallet);

            // 5. 지갑 주소와 개인 키를 DTO에 담아 반환
            return new CreateWalletAddressDto(walletAddress, privateKey);

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("지갑 생성에 실패했습니다.", e);
        }
    }

    /**
     * 사용자의 지갑에 있는 토큰의 이름, 심볼, 개수, 가격 정보를 조회합니다.
     * 토큰 이름과 심볼은 DB(Token 엔티티)에서 가져오고,
     * 토큰 개수는 블록체인에서, 가격은 외부 API에서 조회합니다.
     *
     * @param userSeq 사용자 시퀀스 번호
     * @return 지갑에 있는 토큰 정보 목록
     */
    @Transactional
    public List<WalletTokenInfoDto> getWalletTokenInfo(Integer userSeq) {
        // 1. userSeq로 지갑 주소 조회
        Optional<Wallet> walletOptional = walletRepository.findByUserSeq(userSeq);
        Wallet wallet = walletOptional.orElseThrow(() -> new com.ddiring.backend_asset.common.exception.NotFound("해당하는 지갑을 찾을 수 없습니다."));
        String walletAddress = wallet.getWalletAddress();

        // 2. 지갑에 연결된 모든 토큰 조회 (DB에서 토큰 이름, 심볼, 컨트랙트 주소 가져옴)
        List<Token> tokens = tokenRepository.findByWalletSeq(wallet.getWalletSeq());

        // 3. 각 토큰에 대해 블록체인 잔액과 외부 가격 조회
        return tokens.stream().map(token -> {
            // 블록체인에서 토큰 잔액 조회
            BigInteger balance = getTokenBalance(token.getContractAddress(), walletAddress);

            // 외부 API에서 토큰 가격 조회
            BigDecimal price = externalPriceApi.getTokenPriceInKRW(token.getContractAddress());

            return new WalletTokenInfoDto(
                    token.getTokenName(),      // DB에서 가져온 토큰 이름
                    token.getTokenSymbol(),    // DB에서 가져온 토큰 심볼
                    Convert.fromWei(new BigDecimal(balance), Convert.Unit.ETHER), // ETHER 단위로 변환
                    price
            );
        }).collect(Collectors.toList());
    }

    /**
     * 특정 ERC-20 토큰의 컨트랙트 주소와 지갑 주소를 사용하여 블록체인에서 토큰 잔액을 조회합니다.
     *
     * @param tokenContractAddress 조회할 토큰의 스마트 컨트랙트 주소
     * @param walletAddress 잔액을 조회할 지갑 주소
     * @return 해당 지갑 주소의 토큰 잔액 (BigInteger)
     */
    private BigInteger getTokenBalance(String tokenContractAddress, String walletAddress) {
        // ERC-20 표준의 balanceOf 함수를 호출하기 위한 Function 객체 생성
        Function function = new Function(
                "balanceOf", // ERC-20 표준 함수 이름
                Collections.singletonList(new Address(walletAddress)), // 입력 파라미터: 지갑 주소
                Collections.singletonList(new TypeReference<Uint256>() {})); // 출력 파라미터: Uint256 (잔액)

        // 함수 호출을 위한 데이터 인코딩
        String encodedFunction = FunctionEncoder.encode(function);

        try {
            // 이더리움 노드에 eth_call 요청 전송
            EthCall ethCall = web3j.ethCall(
                            Transaction.createEthCallTransaction(
                                    walletAddress, // from 주소 (여기서는 호출하는 지갑 주소, 실제 트랜잭션은 아님)
                                    tokenContractAddress, // 호출 대상 컨트랙트 주소
                                    encodedFunction), // 인코딩된 함수 데이터
                            DefaultBlockParameterName.LATEST) // 최신 블록에서 조회
                    .send();

            // 응답 값 디코딩
            List<org.web3j.abi.datatypes.Type> results = FunctionReturnDecoder.decode(ethCall.getValue(), function.getOutputParameters());
            if (!results.isEmpty()) {
                return (BigInteger) results.get(0).getValue();
            } else {
                return BigInteger.ZERO; // 잔액이 없는 경우 0 반환
            }
        } catch (IOException e) {
            // 블록체인 통신 중 오류 발생 시 예외 처리
            throw new RuntimeException("블록체인에서 토큰 잔액 조회 실패: " + e.getMessage(), e);
        }
    }
}
