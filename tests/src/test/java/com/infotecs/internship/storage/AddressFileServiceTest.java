package com.infotecs.internship.storage;

import com.infotecs.internship.model.AddressEntry;
import com.infotecs.internship.transport.FileTransport;
import com.infotecs.internship.validation.IpV4Validator;
import com.infotecs.internship.validation.IpValidator;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;

/**
 * Тесты для {@link AddressFileService}.
 */
public class AddressFileServiceTest {

    private IpValidator ipValidator;

    @BeforeMethod
    public void setUp() {
        ipValidator = new IpV4Validator();
    }

    @Test
    public void loadParsesEntriesFromTransport() throws IOException {
        FakeTransport transport = new FakeTransport(
                "{\"addresses\": [{\"domain\": \"example.com\", \"ip\": \"1.2.3.4\"}]}");
        AddressFileService service = new AddressFileService(transport);

        AddressBook book = service.load(ipValidator);

        Assert.assertEquals(book.findIpByDomain("example.com"), java.util.Optional.of("1.2.3.4"));
    }

    @Test
    public void loadWithEmptyAddressesProducesEmptyBook() throws IOException {
        FakeTransport transport = new FakeTransport("{\"addresses\": []}");
        AddressFileService service = new AddressFileService(transport);

        AddressBook book = service.load(ipValidator);

        Assert.assertTrue(book.getAllEntries().isEmpty());
    }

    @Test
    public void saveWritesCurrentBookStateToTransport() throws IOException {
        FakeTransport transport = new FakeTransport("{\"addresses\": []}");
        AddressFileService service = new AddressFileService(transport);
        AddressBook book = service.load(ipValidator);

        book.add("example.com", "192.168.0.1");
        service.save(book);

        // проверяем через повторную загрузку из того же транспорта (round-trip)
        AddressBook reloaded = service.load(ipValidator);
        Assert.assertEquals(reloaded.findIpByDomain("example.com"), java.util.Optional.of("192.168.0.1"));
    }

    @Test
    public void saveAfterRemovePersistsDeletion() throws IOException {
        FakeTransport transport = new FakeTransport(
                "{\"addresses\": [{\"domain\": \"example.com\", \"ip\": \"1.2.3.4\"}]}");
        AddressFileService service = new AddressFileService(transport);
        AddressBook book = service.load(ipValidator);

        book.remove("example.com");
        service.save(book);

        AddressBook reloaded = service.load(ipValidator);
        Assert.assertTrue(reloaded.getAllEntries().isEmpty());
    }

    private static final class FakeTransport implements FileTransport {

        private String content;

        FakeTransport(String initialContent) {
            this.content = initialContent;
        }

        @Override
        public String download() {
            return content;
        }

        @Override
        public void upload(String newContent) {
            this.content = newContent;
        }
    }
}