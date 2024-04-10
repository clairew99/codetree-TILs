import java.io.*;
import java.util.*;

/**
 * 2023 하반기 오후 1번
 * @author eunwoo.lee
 * 
 * 1. 입력
 * 	1-1. 체스판의 사이즈, 기사의 수, 명령의 수를 입력받는다.
 * 	1-2. 체스판의 정보를 입력받는다.
 * 	1-3. 초기 기사들의 정보를 입력받는다.
 * 	1-4. 명령의 수만큼 반복해 명령을 입력받는다.
 * 2. 기사를 이동시킨다
 * 	2-1. 큐를 이용해 기사를 이동시킬 수 있는지 확인 후 이동할 수 있으면 이동
 * 3. 살아남은 기사들의 결과를 계산해 출력한다.
 *
 */

public class Main {
	static BufferedReader br;
	static StringBuilder sb;
	static StringTokenizer st;
	
	static int mapSize, knightNum, cmdNum;
	static int[][] map; 
	static Knight[] knights; // 기사 저장 배열
	static int[] originalHealth; // 데미지 계산을 위해 기존 체력 저장
	static int[] nextRow, nextCol, nextDamage; // 이동할 수 있다면 새로운 이동 위치 저장
	
	// delta 배열 : 상, 우, 하, 왼
	static int[] dr = {-1, 0, 1, 0};
	static int[] dc = {0, 1, 0, -1};
	
	static final int TRAP = 1;
	static final int WALL = 2;
	
	static class Knight{
		int rowIdx, colIdx; // 기사의 현재 좌표
		int shieldHeight, shieldWidth; // 방패 크기
		int health; // 기사의 체력, 받은 데미지
		
		public Knight(int rowIdx, int colIdx, int shieldHeight, int shieldWidth, int health) {
			super();
			this.rowIdx = rowIdx;
			this.colIdx = colIdx;
			this.shieldHeight = shieldHeight;
			this.shieldWidth = shieldWidth;
			this.health = health;
		}

		@Override
		public String toString() {
			return "Knight [rowIdx=" + rowIdx + ", colIdx=" + colIdx + ", shieldHeight=" + shieldHeight
					+ ", shieldWidth=" + shieldWidth + ", health=" + health + "]";
		}
		
	}
	
	public static void setInit() throws IOException{
		// 1-1. 체스판의 사이즈, 기사의 수, 명령의 수를 입력받는다.
		st = new StringTokenizer(br.readLine().trim());
		mapSize = Integer.parseInt(st.nextToken());
		knightNum = Integer.parseInt(st.nextToken());
		cmdNum = Integer.parseInt(st.nextToken());
		
		// 1-2. 체스판의 정보를 입력받는다.
		map = new int[mapSize+1][mapSize+1];
		for (int rowIdx=1; rowIdx<=mapSize; rowIdx++) {
			st = new StringTokenizer(br.readLine().trim());
			for (int colIdx=1; colIdx<=mapSize; colIdx++) {
				map[rowIdx][colIdx] = Integer.parseInt(st.nextToken());
			}
		}
		// 1-3. 초기 기사들의 정보를 입력받는다.		
		knights = new Knight[knightNum+1];
		originalHealth = new int[knightNum+1];
		
		for (int kIdx=1; kIdx<=knightNum; kIdx++) {
			st = new StringTokenizer(br.readLine().trim());
			int r = Integer.parseInt(st.nextToken());
			int c = Integer.parseInt(st.nextToken());
			int h = Integer.parseInt(st.nextToken());
			int w = Integer.parseInt(st.nextToken());
			int k = Integer.parseInt(st.nextToken());
			originalHealth[kIdx] = k;
			knights[kIdx] = new Knight(r, c, h, w, k); // 기사가 받은 초기 데미지는 0
		}
		
//		for (int[] r: map) {
//			for (int c: r) {
//				System.out.print(c+" ");
//			}
//			System.out.println();
//		}
	}
	
	public static void moveKnights(int knightIdx, int direction) {
		// 만약 체스판에 존재하지 않는 기사라면 return
		if (knights[knightIdx].health<=0) return;
		
		// 이동이 가능한 경우, 실제 위치와 체력 업데이트
		if (tryMove(knightIdx, direction)) {
			// 모든 기사에 대해 업데이트
			for (int kIdx=1; kIdx<=knightNum; kIdx++) {
				knights[kIdx].rowIdx = nextRow[kIdx];
				knights[kIdx].colIdx = nextCol[kIdx];
				knights[kIdx].health -= nextDamage[kIdx];
			}
		}
	}
	
	public static boolean tryMove(int knightIdx, int d) {
		// 초기화
		nextRow = new int[knightNum+1];
		nextCol = new int[knightNum+1];
		nextDamage = new int[knightNum+1];
		
		for (int kIdx=1; kIdx<=knightNum; kIdx++) {
			nextRow[kIdx] = knights[kIdx].rowIdx;
			nextCol[kIdx] = knights[kIdx].colIdx;
			nextDamage[kIdx]=0;
		}
		
		// 큐를 이용해 연쇄적으로 탐색
		Queue<Integer> q = new ArrayDeque<>();
		boolean[] isMoved = new boolean[knightNum+1];
		
		q.offer(knightIdx);
		isMoved[knightIdx] = true;
		
		while (!q.isEmpty()) {
			int currentIdx = q.poll();
			// System.out.println("while문:"+currentIdx);
			
			nextRow[currentIdx] += dr[d];
			nextCol[currentIdx] += dc[d];
			
//			System.out.println(nextRow[currentIdx]);
//			System.out.println(nextCol[currentIdx]);
			
			// 경계를 벗어난다면 false
			if (nextRow[currentIdx]<1 || nextCol[currentIdx]<1 || 
					nextRow[currentIdx]+knights[currentIdx].shieldHeight-1>mapSize ||
					nextCol[currentIdx]+knights[currentIdx].shieldWidth-1>mapSize) {
				//System.out.println("경계벗어남");
				return false;
			}
			
			// 대상 기사의 방패 범위 안에 다른 장애물이 있는지 검사
			for (int rowIdx=nextRow[currentIdx]; rowIdx<=nextRow[currentIdx]+knights[currentIdx].shieldHeight-1; rowIdx++) {
				for (int colIdx=nextCol[currentIdx]; colIdx<=nextCol[currentIdx]+knights[currentIdx].shieldWidth-1; colIdx++) {
					if (map[rowIdx][colIdx]==TRAP) {
						//System.out.println("데미지");
						nextDamage[currentIdx]++;
					}
					if (map[rowIdx][colIdx]==WALL) {
						//System.out.println("벽있어서 이동 못함");
						return false;
					}
				}
			}
			
			// 다른 물체나 기사와 만나는 경우, 해당 객체도 이동
			for (int kIdx=1; kIdx<=knightNum; kIdx++) {
				// 해당 기사가 움직이지 않았거나 사라진 경우
				if (isMoved[kIdx] || knights[kIdx].health<=0)
					continue;
				// 좌표 비교 - 서로의 r좌표가 겹치지 않을 때
				if (knights[kIdx].rowIdx > nextRow[currentIdx]+knights[currentIdx].shieldHeight-1 ||
						nextRow[currentIdx] > knights[kIdx].rowIdx + knights[kIdx].shieldHeight-1)
					continue;
				// 좌표 비교 - 서로의 c좌표가 겹치지 않을 때
				if (knights[kIdx].colIdx > nextCol[currentIdx]+knights[currentIdx].shieldWidth-1 ||
						nextCol[currentIdx] > knights[kIdx].colIdx + knights[kIdx].shieldWidth-1)
					continue;
				
				// 이 모든 조건에 걸리지 않았다면 둘이 만난다
				isMoved[kIdx] = true;
				q.offer(kIdx);
			}
		}
		nextDamage[knightIdx] = 0;
		return true;
	}

	public static void main(String[] args) throws Exception{

		br = new BufferedReader(new InputStreamReader(System.in));
		sb = new StringBuilder();
		
		setInit();
		
		// 1-4. 명령의 수만큼 반복해 명령을 입력받는다.
		for (int cmdIdx=0; cmdIdx<cmdNum; cmdIdx++) {
			//System.out.println("명령"+(cmdIdx+1));
			st = new StringTokenizer(br.readLine().trim());
			int knightIdx = Integer.parseInt(st.nextToken());
			int direction = Integer.parseInt(st.nextToken());
			// 2. 기사를 이동시킨다
			moveKnights(knightIdx, direction);
			
		}
		// 3. 살아남은 기사들의 결과를 계산한다.
		int damageSum = 0;
		for (int idx=1; idx<=knightNum; idx++) {
			
			// System.out.println(knights[idx].health+" "+originalHealth[idx]);
			if (knights[idx].health>0) {
				damageSum += originalHealth[idx]-knights[idx].health;
			}
		}
		
		// 4. 출력
		sb.append(damageSum);
		System.out.println(sb);
		

	}

}