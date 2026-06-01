import 'package:flutter/material';
import 'package:camera/camera.dart';

void main() async {
  WidgetsFlutterBinding.ensureInitialized();
  
  // 카메라 목록 획득
  List<CameraDescription> cameras = [];
  try {
    cameras = await availableCameras();
  } catch (e) {
    print('카메라가 지원되지 않거나 접근 불가합니다: $e');
  }

  runApp(ShotDoApp(cameras: cameras));
}

class ShotDoApp extends StatelessWidget {
  final List<CameraDescription> cameras;

  const ShotDoApp({Key? key, required this.cameras}) : super(key: key);

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'ShotDo',
      theme: ThemeData(
        brightness: Brightness.dark,
        primaryColor: const Color(0xFF10B981),
        scaffoldBackgroundColor: const Color(0xFF0A0C10),
        colorScheme: ColorScheme.fromSeed(
          seedColor: const Color(0xFF10B981),
          brightness: Brightness.dark,
        ),
        useMaterial3: true,
      ),
      home: DashboardScreen(cameras: cameras),
    );
  }
}

class DashboardScreen extends StatefulWidget {
  final List<CameraDescription> cameras;
  const DashboardScreen({Key? key, required this.cameras}) : super(key: key);

  @override
  State<DashboardScreen> createState() => _DashboardScreenState();
}

class _DashboardScreenState extends State<DashboardScreen> {
  int _streak = 3;
  List<Map<String, dynamic>> _todos = [
    {'text': 'Flutter 카메라 UI 구성하기', 'completed': true},
    {'text': '백엔드 API 호출 로직 연결', 'completed': false},
  ];

  @override
  Widget build(BuildContext context) {
    final bool isAllCompleted = _todos.every((todo) => todo['completed']);

    return Scaffold(
      appBar: AppBar(
        title: const Text('ShotDo', style: TextStyle(fontWeight: FontWeight.bold)),
        actions: [
          Padding(
            padding: const EdgeInsets.symmetric(horizontal: 16.0),
            child: Chip(
              label: Text('🔥 연속 $_streak일째'),
              backgroundColor: const Color(0xFF10B981).withOpacity(0.2),
            ),
          )
        ],
      ),
      body: Padding(
        padding: const EdgeInsets.all(16.0),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            const Text(
              '오늘의 투두',
              style: TextStyle(fontSize: 20, fontWeight: FontWeight.bold),
            ),
            const SizedBox(height: 12),
            Expanded(
              child: ListView.builder(
                itemCount: _todos.length,
                itemBuilder: (context, index) {
                  final todo = _todos[index];
                  return Card(
                    color: const Color(0xFF121620),
                    child: ListTile(
                      title: Text(
                        todo['text'],
                        style: TextStyle(
                          decoration: todo['completed']
                              ? TextDecoration.lineThrough
                              : null,
                          color: todo['completed'] ? Colors.grey : Colors.white,
                        ),
                      ),
                      leading: Checkbox(
                        value: todo['completed'],
                        onChanged: (val) {
                          setState(() {
                            _todos[index]['completed'] = val;
                          });
                        },
                      ),
                    ),
                  );
                },
              ),
            ),
            const SizedBox(height: 16),
            SizedBox(
              width: double.infinity,
              height: 56,
              child: ElevatedButton.icon(
                onPressed: isAllCompleted && widget.cameras.isNotEmpty
                    ? () {
                        // TODO: 카메라 촬영 화면(CameraScreen)으로 네비게이션
                        ScaffoldMessenger.of(context).showSnackBar(
                          const SnackBar(content: Text('카메라 촬영을 시작합니다.')),
                        );
                      }
                    : null,
                icon: const Icon(Icons.camera_alt),
                label: const Text('사진 촬영 후 달력 채우기', style: TextStyle(fontSize: 16)),
                style: ElevatedButton.styleFrom(
                  backgroundColor: const Color(0xFF10B981),
                  foregroundColor: Colors.white,
                  disabledBackgroundColor: const Color(0xFF1A1F2C),
                ),
              ),
            ),
          ],
        ),
      ),
    );
  }
}
