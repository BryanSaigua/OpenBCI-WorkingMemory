import socket
import threading
import sys
import pickle
import time

from time import sleep

class Server():
    def __init__(self, host="192.168.0.148", port=5000):
        self.sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        self.sock.bind((str(host), int(port)))
        self.sock.listen(10)
        self.sock.setblocking(False)

        self.clients = []
        accept = threading.Thread(target=self.aceptarCon)
        process = threading.Thread(target=self.processarCon)

        accept.daemon = True
        accept.start()

        process.daemon = True
        process.start()

        while True:
            msg = input('')
            if msg == 'salir':
                self.sock.close()   
                sys.exit()
            else:
                pass

    def aceptarCon(self):
        print("AceptarCon iniciado")
        while True:
            try:
                conn, addr = self.sock.accept()
                conn.setblocking(False)
                self.clients.append({
                    'client': conn,
                    'data': {}
                })
                print("Se conecto un cliente: ", addr)
            except:
                pass

    def processarCon(self):
        print("ProcessarCon iniciado")
        while True:
            if len(self.clients) > 0:
                for c in self.clients:
                    try:
                        data = c['client'].recv(1024)
                        if (data.decode("UTF-8") == 'enviar'):
                            self.activated = True
                        if (self.activated):
                            while True:
                                msg = "[1800000,120000,100000,80000,60000,14000,12000,10000]".encode("UTF-8")
                                print('Enviando...',msg)
                                c['client'].send((len(msg).to_bytes(2, byteorder='big')))
                                c['client'].send((msg))
                                time.sleep(0.05)

                    except: 
                        pass
    
    #def send(self, senal):
        # enviar el cliente la senal

Server()
#server = Server()

#while(reading singnals):
#    if (read sig):
#        server.send('signhafasd')


