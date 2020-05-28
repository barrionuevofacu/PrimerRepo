using GeoCoordinatePortable;
using IBM.Cloud.SDK.Core.Authentication.Iam;
using IBM.Cloud.SDK.Core.Http;
using IBM.Watson.VisualRecognition.v3;
using IBM.Watson.VisualRecognition.v3.Model;
using System;
using System.Collections.Generic;
using System.Globalization;
using System.IO;
using System.Linq;
using System.Threading.Tasks;
using WebApiObjetos.Domain;
using WebApiObjetos.Models.Entities;
using WebApiObjetos.Models.Repositories.Interfaces;
using WebApiObjetos.Services.Interfaces;

namespace WebApiObjetos.Services
{
    public class LocationService : ILocationService
    {
        private ILocationRepository locationRepo;
        private IImageRepository imageRepo;
        private IUserRepository userRepo;
        public LocationService(ILocationRepository locationRepo, IImageRepository imageRepo, IUserRepository userRepo)
        {
            this.locationRepo = locationRepo;
            this.imageRepo = imageRepo;
            this.userRepo = userRepo;
        }


        public async Task<bool> DeleteLocation(int userId, int locationId)
        {
            return await locationRepo.deleteLocation(userId, locationId);
        }

        public async Task<LocationDTO> GetLocation(int userId, int locationId)
        {
            var result = await locationRepo.FindBy(x => x.UserId.Equals(userId) && x.Id.Equals(locationId));
            if (result.Count > 0)
                return result.First().toDto();
            return null;
        }


        public async Task<List<LocationDTO>> GetLocations(int userId)
        {
            var locations = await locationRepo.FindBy(x => x.UserId.Equals(userId));
            List<LocationDTO> locationsDTO = new List<LocationDTO>();
            foreach (Location location in locations)
            {
                locationsDTO.Add(location.toDto());
            }
            return locationsDTO;
        }

        public async Task<List<LocationDTO>> GetLocationsById(List<String> locations)
        {
            List<LocationDTO> result = new List<LocationDTO>();
            foreach (String l in locations)
            {
                int id;
                Int32.TryParse(l.Split(":")[1], out id);
                var location = await locationRepo.FindBy(x => x.Id.Equals(id));
                result.Add(location.First().toDto());
            }
            return result;
        }

        public async Task<LocationDTO> AddLocation(LocationDTO locationDTO)
        {
            try
            {
                if (locationDTO.ImageId == 0)
                    locationDTO.ImageId = null;
                var location = locationDTO.ToEntity();
                location.InsertDate = DateTime.Today;
                if (!locationDTO.IsSearch)
                {
                    var imagen = imageRepo.FindBy(x => x.Id == location.ImageId).Result.FirstOrDefault();
                    List<Image> images = await BuscarParecidos(imagen);
                    foreach (Image i in images)
                    {
                        var loc = locationRepo.FindBy(x => x.ImageId == i.Id).Result.FirstOrDefault();
                        if (loc != null && loc.IsSearch)
                        {
                            var user = userRepo.FindBy(x => x.Id == loc.UserId).Result.FirstOrDefault();
                            if (user.Email != null && !user.Email.Equals(""))
                                EnviarMail(user.Email, "Quizás vieron a tu perro", "Un usuario subió una foto de un perro similar al que estás buscando. \n" +
                                    "Buscá la publicación con el id "+ loc.Id+" y chequeá si es el tuyo.\n" +
                                    "Mucha suerte!");
                        }
                    }
                }
                return (await locationRepo.Add(location)).toDto();
            }
            catch (Exception e)
            {
                return null;
            }
        }

        public async Task<bool> UpdateLocation(LocationDTO location)
        {
            var exists = await locationRepo.Any(x => x.Id.Equals(location.Id) && x.UserId.Equals(location.UserId));
            if (exists)
            {
                if (location.ImageId == 0)
                    location.ImageId = null;
                await locationRepo.Update(location.ToEntity());
                return true;
            }
            return false;
        }

        public async Task<ImageDTO> AddImage(ImageDTO image)
        {
            IamAuthenticator authenticator = new IamAuthenticator(apikey: "FwMOpgXBoVSnG4SYrbduANUaCfNq-m35b-9XxMoVnSYJ");

            VisualRecognitionService visualRecognition = new VisualRecognitionService("2018-03-19", authenticator);
            visualRecognition.SetServiceUrl("https://gateway.watsonplatform.net/visual-recognition/api");
            DetailedResponse<ClassifiedImages> result1;
            string path = @"/tesistemp/MyTest.jpeg";
            if (File.Exists(path)){
                File.Delete(path);
            }

            //GUARDA IMAGEN EN DIRECTORIO
            byte[] imageBytes = Convert.FromBase64String(image.Picture);
            using (var imageFile = new FileStream(path, FileMode.Create))
            {
                imageFile.Write(imageBytes, 0, imageBytes.Length);
                imageFile.Flush();
            }

            //PREDICE LA RAZA
            using (FileStream fs = File.OpenRead(path))
            {
                using (MemoryStream ms = new MemoryStream())
                {
                    fs.CopyTo(ms);
                    result1 = visualRecognition.Classify(
                        //url: "https://img.pixers.pics/pho_wat(s3:700/FO/48/14/15/73/700_FO48141573_3b497c03f0d6755bb5657b67149c578d.jpg,700,507,cms:2018/10/5bd1b6b8d04b8_220x50-watermark.png,over,480,457,jpg)/vinilos-para-armario-alaskan-malamute-en-la-nieve.jpg.jpg",
                        imagesFilename: "MyTest.jpeg",
                        imagesFile: ms,
                        classifierIds: new List<string>() { "DefaultCustomModel_1975643640" },
                        threshold: 0.0f,
                        owners: new List<string>() { "me" }
                    );
                }
            }
            //  The result object
            var responseHeaders = result1.Headers;  //  The response headers
            var responseJson = result1.Result;    //  The raw response JSON
            var statusCode = result1.StatusCode;
            List<ClassResult> razas = responseJson.Images.FirstOrDefault().Classifiers.FirstOrDefault().Classes;
            int razasAGuardar = 3;
            if (razas.Count < 3)
                razasAGuardar = razas.Count;

            Console.WriteLine(result1.Response);
            razas = razas.OrderByDescending(o => o.Score).ToList();
            for (int i = 0; i < razasAGuardar; i++)
            {
                var razaI = razas.ElementAt(i)._Class;
                var porcentaje = razas.ElementAt(i).Score;
                var raza = razaI.Split(".")[0];
                if (i == 0)
                    image.raza1 = raza;
                else if (i == 1)
                    image.raza2 = raza;
                else
                    image.raza3 = raza;

                Console.WriteLine("Cargando raza: " + raza + ". Con una precision de: " + porcentaje);
            }

            try
            {
                ImageDTO imagen = (await imageRepo.Add(image.ToEntity())).toDto();
                return imagen;
            }
            catch (Exception e)
            {
                return null;
            }
        }

        public void EnviarMail(string destinatario, string asunto, string mensaje)
        {
            System.Net.Mail.MailMessage msg = new System.Net.Mail.MailMessage();
            msg.To.Add(destinatario);
            msg.Subject = asunto;
            msg.SubjectEncoding = System.Text.Encoding.UTF8;
            msg.Body = mensaje;
            msg.BodyEncoding = System.Text.Encoding.UTF8;
            msg.From = new System.Net.Mail.MailAddress("tesisperrosperdidos@gmail.com");

            //Cliente Mail
            System.Net.Mail.SmtpClient cliente = new System.Net.Mail.SmtpClient();
            cliente.Credentials = new System.Net.NetworkCredential("tesisperrosperdidos@gmail.com", "tesisperros");
            cliente.EnableSsl = true;
            cliente.Port = 587;
            cliente.Host = "smtp.gmail.com";
            try
            {
                cliente.Send(msg);
                Console.WriteLine("Correo enviado con éxito");
            }
            catch(Exception e)
            {
                Console.WriteLine("Error al enviar el mail");
            }
            finally
            {
                msg.Dispose();
                cliente.Dispose();
            }
        }

        public async Task<List<Image>> BuscarParecidos(Image image)
        {
            return imageRepo
                                        .FindBy(x =>
                                                    (x.raza1 != null && image.raza1 != null && x.raza1 == image.raza1) ||
                                                    (x.raza1 != null && image.raza2 != null && x.raza1 == image.raza2) ||
                                                    (x.raza1 != null && image.raza3 != null && x.raza1 == image.raza3) ||
                                                    (x.raza2 != null && image.raza1 != null && x.raza2 == image.raza1) ||
                                                    (x.raza2 != null && image.raza2 != null && x.raza2 == image.raza2) ||
                                                    (x.raza2 != null && image.raza3 != null && x.raza2 == image.raza3) ||
                                                    (x.raza3 != null && image.raza1 != null && x.raza3 == image.raza1) ||
                                                    (x.raza3 != null && image.raza2 != null && x.raza3 == image.raza2) ||
                                                    (x.raza3 != null && image.raza3 != null && x.raza3 == image.raza3)
                                        )
                                        .Result
                                        .ToList();
        }

        public async Task<List<LocationDTO>> Buscar(LocationDTO location)
        {
            try
            {
                if (location.ImageId == 0 || location.ImageId == null)
                    return null;
                location.IsSearch = true;
                await AddLocation(location);
                Image image = await imageRepo.GetById((int)location.ImageId);
                List<Image> images = await BuscarParecidos(image);
                List<LocationDTO> result = new List<LocationDTO>();
                foreach (Image i in images)
                {
                    if (i.Id != location.ImageId)
                    {
                        var loc = locationRepo.FindBy(x => x.ImageId == i.Id).Result.FirstOrDefault();
                        if (loc != null && !loc.IsSearch && loc.UserId != location.UserId && getDistance(loc.toDto(), location) < 3000)
                            result.Add(loc.toDto());
                    }
                }
                return result;
            }
            catch (Exception e)
            {
                return null;
            }
        }
        public double getDistance(LocationDTO loc1, LocationDTO loc2)
        {
            var coor1 = getPoints(loc1.Coordinates).First();
            var coor2 = getPoints(loc2.Coordinates).First();
            return coor1.GetDistanceTo(coor2);
        }
        public async Task<ImageDTO> GetImage(int imageId)
        {
            var result = await imageRepo.FindBy(x => x.Id.Equals(imageId));

            if (result.Count > 0)
                return result.First().toDto();
            return null;
        }

        public async Task<List<LocationDTO>> GetLocationsNoPropias(int userId)
        {
            var locations = await locationRepo.FindBy(x => !x.UserId.Equals(userId) && !x.IsSearch);

            List<LocationDTO> locationsDTO = new List<LocationDTO>();
            foreach (Location location in locations)
            {
                locationsDTO.Add(location.toDto());
            }
            return locationsDTO;
        }


        public async Task<List<LocationDTO>> getLocationsInArea(string coordinates, int userId)
        {
            var userLocations = await this.GetLocationsNoPropias(userId);

            List<GeoCoordinate> AreaCoordinates = getPoints(coordinates);

            List<LocationDTO> returnList = new List<LocationDTO>();

            foreach (LocationDTO location in userLocations)
            {
                var isIn = true;
                List<GeoCoordinate> locationCoordinates = getPoints(location.Coordinates);
                int i = 0;
                while (i < locationCoordinates.Count && isIn)
                {
                    var point = locationCoordinates[i];
                    isIn = IsInPolygon(AreaCoordinates, point);
                    i++;
                }
                if (isIn)
                    returnList.Add(location);
            }
            return returnList;
        }

        //x=longitude
        //y=latitude
        // Return True if the point is in the polygon.
        private bool IsInPolygon(List<GeoCoordinate> AreaCoordinates, GeoCoordinate point)
        {
            // Get the angle between the point and the
            // first and last vertices.
            int max_point = AreaCoordinates.Count - 1;
            double total_angle = GetAngle(
                AreaCoordinates[max_point].Longitude, AreaCoordinates[max_point].Latitude,
                point.Longitude, point.Latitude,
                AreaCoordinates[0].Longitude, AreaCoordinates[0].Latitude);

            for (int i = 0; i < max_point; i++)
            {
                total_angle += GetAngle(
                    AreaCoordinates[i].Longitude, AreaCoordinates[i].Latitude,
                    point.Longitude, point.Latitude,
                    AreaCoordinates[i + 1].Longitude, AreaCoordinates[i + 1].Latitude);
            }

            return (Math.Abs(total_angle) > 1);
        }


        private static double GetAngle(double Ax, double Ay,
            double Bx, double By, double Cx, double Cy)
        {
            double dot_product = DotProduct(Ax, Ay, Bx, By, Cx, Cy);

            double cross_product = CrossProductLength(Ax, Ay, Bx, By, Cx, Cy);

            return (double)Math.Atan2(cross_product, dot_product);
        }

        private static double DotProduct(double Ax, double Ay,
            double Bx, double By, double Cx, double Cy)
        {
            // Get the vectors' coordinates.
            double BAx = Ax - Bx;
            double BAy = Ay - By;
            double BCx = Cx - Bx;
            double BCy = Cy - By;

            return (BAx * BCx + BAy * BCy);
        }

        private static double CrossProductLength(double Ax, double Ay,
            double Bx, double By, double Cx, double Cy)
        {
            // Get the vectors' coordinates.
            double BAx = Ax - Bx;
            double BAy = Ay - By;
            double BCx = Cx - Bx;
            double BCy = Cy - By;

            return (BAx * BCy - BAy * BCx);
        }

        private List<GeoCoordinate> getPoints(String locationsString)
        {
            List<GeoCoordinate> coordinatesList = new List<GeoCoordinate>();

            foreach (String location in locationsString.Split(';'))
            {
                if (!location.Equals(""))
                {
                    String lat = location.Split(",")[0];
                    String lng = location.Split(",")[1];
                    double a = Double.Parse(lat, System.Globalization.CultureInfo.InvariantCulture);
                    double b = Double.Parse(lng, System.Globalization.CultureInfo.InvariantCulture);
                    GeoCoordinate latLng = new GeoCoordinate();
                    latLng = new GeoCoordinate(a, b);
                    coordinatesList.Add(latLng);
                }
            }
            return coordinatesList;
        }

        public async Task<bool> ContactInformador(int userId, int imageId)
        {
            try
            {
                var buscador = userRepo.FindBy(x => x.Id.Equals(userId)).Result.FirstOrDefault();
                var informadorId = imageRepo.FindBy(x => x.Id == imageId).Result.FirstOrDefault().UserId;
                var informador = userRepo.FindBy(x => x.Id == informadorId).Result.FirstOrDefault();
                var mensaje = "Un usuario vió tu publicación y desea comunicarse con vos! " +
                    "Contactalo mediante telefono al: " + buscador.Telefono + " o a su correo: " + buscador.Email + " . \n" +
                    "Muchas gracias!";
                EnviarMail(informador.Email, "Un usuario desea contactarte", mensaje);
                return true;
            }
            catch (Exception e)
            {
                return false;
            }

        }
    }
}
